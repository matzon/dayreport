package dk.matzon.dayreport.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.matzon.dayreport.application.service.DataGathererImpl;
import dk.matzon.dayreport.application.service.GeocodingServiceImpl;
import dk.matzon.dayreport.domain.DataGatherer;
import dk.matzon.dayreport.domain.GeocodingService;
import dk.matzon.dayreport.domain.Repository;
import dk.matzon.dayreport.domain.model.DayReport;
import dk.matzon.dayreport.domain.model.DayReportEntry;
import dk.matzon.dayreport.domain.model.Region;
import dk.matzon.dayreport.infrastructure.persistence.DayReportEntryRepositoryImpl;
import dk.matzon.dayreport.infrastructure.persistence.DayReportRepositoryImpl;
import dk.matzon.dayreport.infrastructure.persistence.HibernateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Brian Matzon <brian@matzon.dk>
 */
public class DayReportApp {
    private final Logger LOGGER = LogManager.getLogger(DayReportApp.class);

    private final Properties properties;

    private final ScheduledExecutorService scheduledExecutorService;

    private Repository<DayReport> dayReportRepository;
    private Repository<DayReportEntry> dayReportEntryRepository;

    private DataGatherer dataGatherer;

    private GeocodingService geocodingService;

    private volatile boolean active;

    public DayReportApp() {
        active = false;
        properties = new Properties();
        scheduledExecutorService = Executors.newScheduledThreadPool(5);
    }

    private void init() throws IOException {
        // prepare directories
        new File("data/db").mkdirs();
        new File("data/reports").mkdirs();

        InputStream configInputStream = DayReportApp.class.getResourceAsStream("/config.properties");
        properties.load(configInputStream);

        // configure db
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        dayReportRepository = new DayReportRepositoryImpl(sessionFactory);
        dayReportEntryRepository = new DayReportEntryRepositoryImpl(sessionFactory);

        // configure data gather
        dataGatherer = new DataGathererImpl(scheduledExecutorService, dayReportRepository, properties);
        dataGatherer.initialize();

        // configure geocoding service
        geocodingService = new GeocodingServiceImpl(properties, (DayReportEntryRepositoryImpl) dayReportEntryRepository, scheduledExecutorService);
        geocodingService.initialize();

        // configure backup
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                HibernateUtil.backup();
            }
        }, 1, Long.parseLong(properties.getProperty("app.backupperiod")), TimeUnit.MINUTES);

        active = true;
    }

    private void prepareShutdown() {
        LOGGER.info("prepareShutdown invoked");
        scheduledExecutorService.shutdown();
        active = false;
    }

    public boolean isRunning() {
        return dataGatherer.isRunning();
    }

    private void handleCommand(String _command) throws IOException {
        String[] command = _command.trim().toLowerCase().split(" ");
        Region region = Region.FYN;
        if (command.length > 1) {
            try {
                region = Region.valueOf(command[1]);
            } catch (Exception e) {
                LOGGER.warn("No such region: " + command[1] + ", defaulting to FYN");
            }
        }

        List<DayReport> all;
        switch (command[0]) {
            case "quit":
                prepareShutdown();
                break;
            case "gather":
                dataGatherer.downloadData(null, null, Region.FYN);
                break;
            case "gathermonth":
                Date from = DateUtils.truncate(new Date(), Calendar.MONTH);
                Date to = DateUtils.addMonths(from, 1);
                System.out.println("Downloading reports from " + from + " to " + to);
                dataGatherer.downloadData(from, to, region);
                break;
            case "gatherlmonth":
                Date lmfrom = DateUtils.addMonths(DateUtils.truncate(new Date(), Calendar.MONTH), -1);
                Date lmto = DateUtils.addMonths(lmfrom, 1);
                System.out.println("Downloading reports from " + lmfrom + " to " + lmto);
                dataGatherer.downloadData(lmfrom, lmto, region);
                break;
            case "list":
                all = dayReportRepository.findAll();
                for (DayReport dayReport : all) {
                    System.out.println(dayReport);
                }
                break;
            case "latest":
                DayReport latest = dayReportRepository.findLatest();
                System.out.println(latest);
                break;
            case "types":
                all = dayReportRepository.findAll();
                Set<String> types = new HashSet<>();
                for (DayReport dayReport : all) {
                    List<DayReportEntry> dayReportEntries = dayReport.getDayReportEntries();
                    for (DayReportEntry dayReportEntry : dayReportEntries) {
                        types.add(dayReportEntry.getType());
                    }
                }
                for (String type : types) {
                    System.out.println(type);
                }
                break;
            case "backup":
                HibernateUtil.backup();
                break;
            case "dump":
                dumpReports();
                break;
            case "geolookup":
                geocodingService.processEntriesMissingGeocodeInformation();
            case "geoerror":
                List<DayReportEntry> failingGeocodeEntries = geocodingService.findFailingGeocodeEntries();
                for (DayReportEntry failingGeocodeEntry : failingGeocodeEntries) {
                    System.out.println("Failed geocode for zipCode: " + failingGeocodeEntry.getZipCode() + ", city: " + failingGeocodeEntry.getCity() + ", location: " + failingGeocodeEntry.getLocation());
                }
                break;
            default:
                System.out.println("Unknown command '" + command[0] + "'");
                break;
        }
    }

    private void dumpReports() throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ObjectMapper objectMapper = new ObjectMapper();

        List<DayReport> all = dayReportRepository.findAll();
        for (DayReport dayReport : all) {
            Date reportDate = dayReport.getDate();
            Region region = dayReport.getRegion();
            String path = "data/reports/" + region.name() + "/";
            ensurePath(path);
            File file = new File(path + sdf.format(reportDate) + ".json");
            System.out.println("Dumping " + dayReport.getDate() + " to " + file.getAbsolutePath());
            objectMapper.writeValue(file, dayReport);
        }
    }

    private void ensurePath(String _path) {
        new File(_path).mkdirs();
    }

    private void shutdown() {
        try {
            scheduledExecutorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException _e) {
            LOGGER.warn("Exception while waiting for scheduled executor service to terminate: " + _e.getMessage());
        }
        HibernateUtil.getSessionFactory().close();
        dataGatherer.shutdown();
    }

    private String timeForNextDataJob() {
        long delay = dataGatherer.getTimeForNextJob();
        String formattedDelay = "00:00";
        if (delay > 0) {
            formattedDelay = DurationFormatUtils.formatDuration(delay, "mm:ss");
        }
        return formattedDelay;
    }


    /**
     * Main entry point for application. General flow:
     */
    public static void main(String[] args) throws Exception {
        DayReportApp app = new DayReportApp();
        app.init();

        System.out.println("DayReportApp running...");

        Scanner input = new Scanner(System.in);
        while (app.isRunning()) {
            System.out.printf("(" + app.timeForNextDataJob() + ") $> ");
            String command = input.nextLine();
            app.handleCommand(command);
        }

        app.shutdown();
    }
}
