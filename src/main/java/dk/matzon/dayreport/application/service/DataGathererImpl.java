package dk.matzon.dayreport.application.service;

import dk.matzon.dayreport.application.service.handlers.RegionHandlerFactory;
import dk.matzon.dayreport.domain.DataGatherer;
import dk.matzon.dayreport.domain.RegionHandler;
import dk.matzon.dayreport.domain.Repository;
import dk.matzon.dayreport.domain.model.DayReport;
import dk.matzon.dayreport.domain.model.Region;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import javax.annotation.Nonnull;
import javax.net.ssl.SSLContext;
import javax.persistence.PersistenceException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public class DataGathererImpl implements DataGatherer {

    private final Logger LOGGER = LogManager.getLogger(DataGathererImpl.class);
    private final ScheduledExecutorService scheduledExecutorService;
    private final Repository<DayReport> repository;
    private final Properties properties;

    private ScheduledFuture<?> scheduledFuture;
    private int errorCount = 0;

    public DataGathererImpl(ScheduledExecutorService _scheduledExecutorService, Repository<DayReport> _repository, Properties _properties) {
        scheduledExecutorService = _scheduledExecutorService;
        repository = _repository;
        properties = _properties;
    }

    @Override
    public void init() {
        LOGGER.info("initializing");
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                downloadData(null, null, Region.FYN);
            }
        }, Long.parseLong(properties.getProperty("datagatherer.datadelay")), Long.parseLong(properties.getProperty("datagatherer.dataperiod")), TimeUnit.MINUTES);
    }

    @Override
    public void shutdown() {
        LOGGER.info(String.format("shutting down [errorCount: %d]", errorCount));
        scheduledFuture.cancel(true);
    }

    @Override
    public boolean isRunning() {
        return !scheduledFuture.isCancelled();
    }

    @Override
    public long getTimeForNextJob() {
        if (scheduledFuture != null) {
            return scheduledFuture.getDelay(TimeUnit.MILLISECONDS);
        }
        return -1;
    }

    @Override
    public synchronized void downloadData(Date _from, Date _to, @Nonnull Region _region) {
        String body = null;
        CloseableHttpClient client = null;

        // anything in this flow, which is out of order, should result in error count increase due to exceptions being thrown
        try {
            client = configureClient();

            // don't run if we already have the latest report
            Date to = (_to == null) ? new Date() : _to;
            Date from = (_from == null) ? lastDayReportDate() : _from;
            if (from == null) {
                from = DateUtils.addDays(to, -2);
            }

            if (DateUtils.isSameDay(to, from)) {
                LOGGER.info("Skipping data gather already have latest report");
            }

            RegionHandler handlerForRegion = RegionHandlerFactory.getHandlerForRegion(_region, properties);

            if (handlerForRegion != null) {
                // download the overview page
                String reportOverviewPage = handlerForRegion.downloadReportOverviewPage(client, from, to);

                // extract the individual report links
                List<String> reportLinks = handlerForRegion.extractReportLinksFromHTML(reportOverviewPage);

                // download each report
                List<String> bodyReports = handlerForRegion.downloadHTMLReports(client, reportLinks);

                // extract a dayreport entity from the reports
                List<DayReport> dayReports = handlerForRegion.extractReportsFromHTML(bodyReports);

                // and we're almost done
                persist(dayReports);
            } else {
                LOGGER.warn("No handler found for region: " + _region);
            }
            errorCount = 0;
        } catch (Exception _e) {
            LOGGER.warn("Exception occurred while executing main block of datagatherer: " + _e.getMessage(), _e);
            if (body != null) {
                LOGGER.debug(body);
            }
            if (++errorCount == Integer.valueOf(properties.getProperty("datagatherer.maxerrorcount"))) {
                shutdown();
            }
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException _e) {
                    _e.printStackTrace();
                }
            }
        }
    }

    private Date lastDayReportDate() {
        DayReport latest = repository.findLatest();
        if (latest != null) {
            return latest.getDate();
        }
        return null;
    }

    private CloseableHttpClient configureClient() {
        CloseableHttpClient client = null;
        try {
            // setup
            BasicCookieStore cookieStore = new BasicCookieStore();

            // allow self-signed and untrusted certs
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);

            client = HttpClientBuilder.create()
                    .setDefaultCookieStore(cookieStore)
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .setSSLSocketFactory(sslConnectionSocketFactory)
                    .build();
        } catch (Exception e) {
            LOGGER.error("Exception initializing HttpClient: " + e.getMessage(), e);
        }
        return client;
    }

    private void persist(List<DayReport> _dayReports) {
        for (DayReport dayReport : _dayReports) {
            try {
                repository.save(dayReport);
            } catch (PersistenceException pe) {
                if (pe.getCause().getClass().isAssignableFrom(ConstraintViolationException.class)) {
                    LOGGER.info("Skipping existing report: " + dayReport.getDate());
                } else {
                    LOGGER.error("Unexpected persistence exception: " + pe.getMessage(), pe);
                }

            }
        }
    }

}
