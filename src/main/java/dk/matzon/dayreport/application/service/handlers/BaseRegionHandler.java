package dk.matzon.dayreport.application.service.handlers;

import dk.matzon.dayreport.domain.RegionHandler;
import dk.matzon.dayreport.domain.model.DayReport;
import dk.matzon.dayreport.domain.model.DayReportEntry;
import dk.matzon.dayreport.domain.model.Region;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Map.*;
import static java.util.stream.Collectors.*;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public class BaseRegionHandler implements RegionHandler {
    private final Logger LOGGER = LogManager.getLogger(BaseRegionHandler.class);

    private Region region;
    private Properties properties;

    public BaseRegionHandler(Region _region, Properties _properties) {
        region = _region;
        properties = _properties;
    }

    @Override
    public String downloadReportOverviewPage(CloseableHttpClient _client, Date _from, Date _to) throws Exception {
        String body = "";

        try {

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String formatFrom = sdf.format(_from);
            String formatTo = sdf.format(_to);

            LOGGER.info("Downloading report from " + formatFrom + " to " + formatTo);

            // list of reports
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("__EVENTTARGET", "Archivepagecontrol2:Show"));
            nameValuePairs.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
            nameValuePairs.add(new BasicNameValuePair("Archivepagecontrol2:FromDatePicker:_ctl0", formatFrom));
            nameValuePairs.add(new BasicNameValuePair("Archivepagecontrol2:ToDatePicker:_ctl0", formatTo));
            nameValuePairs.add(new BasicNameValuePair("Generictopmenu2:SearchBox1:SearchBoxTextBox", "INDTAST SØGEORD"));
            String reportOverviewUrl = getReportOverviewUrl();
            HttpUriRequest reportsRequest = RequestBuilder.post(reportOverviewUrl)
                    .setEntity(new UrlEncodedFormEntity(nameValuePairs))
                    .build();

            CloseableHttpResponse response = _client.execute(reportsRequest);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                body = EntityUtils.toString(entity);
            } else {
                logWithBody(response, "Unable to get reports");
            }
        } catch (Exception _e) {
            LOGGER.error("Exception while download report index page: " + _e.getMessage(), _e);
            _client.close();
        }
        return body;
    }

    private String getReportOverviewUrl() {
        String baseUrl = properties.getProperty("datagatherer.dayreports.baseurl");
        String guid = properties.getProperty("datagatherer.dayreports." + region.name().toLowerCase() + ".guid");
        return String.format("%s&NRNODEGUID=%%7b%s%%7b", baseUrl, guid);
    }

    @Override
    public List<String> extractReportLinksFromHTML(String _reportOverviewPage) throws Exception {
        List<String> result = new ArrayList<>();

        if (!_reportOverviewPage.isEmpty()) {
            Document dom = Jsoup.parse(_reportOverviewPage);
            Elements elements = dom.select("[href~=Doegnrapporter/Uddrag");
            for (Element element : elements) {
                String href = element.attr("href");
                result.add("https://www.politi.dk" + href);
            }
        }
        return result;

    }

    @Override
    public List<String> downloadHTMLReports(CloseableHttpClient _client, List<String> _reportLinks) throws Exception {
        List<String> dayreportsHTML = new ArrayList<>();

        for (String reportLink : _reportLinks) {
            LOGGER.info("Downloading report: " + reportLink);
            System.out.println("Downloading report: " + reportLink);
            HttpUriRequest request = RequestBuilder.get(reportLink).build();
            CloseableHttpResponse response = _client.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                String body = EntityUtils.toString(entity);
                dayreportsHTML.add(body);
            }
            pause(100);
        }

        return dayreportsHTML;

    }

    @Override
    public List<DayReport> extractReportsFromHTML(List<String> _bodyReports) throws Exception {
        List<DayReport> dayReports = new ArrayList<>();

        for (String bodyReport : _bodyReports) {

            DayReport dayReport = new DayReport();
            dayReport.setRegion(region);

            Document dom = Jsoup.parse(bodyReport);

            // get paragraphs with info, at least it's an ID ...
            Elements reportEntries = dom.getElementById("SS05BPageControl_SS05BXMLliste1").getElementsByTag("p");

            for (Element reportEntry : reportEntries) {
                DayReportEntry dayReportEntry = parseDayReportEntry(reportEntry, dayReport);
                if (dayReportEntry != null) {
                    dayReport.getDayReportEntries().add(dayReportEntry);
                }
            }

            if (!dayReport.getDayReportEntries().isEmpty()) {
                Date ended = findEndedDate(dayReport);
                DateUtils.truncate(ended, Calendar.HOUR);
                dayReport.setDate(ended);

                dayReports.add(dayReport);
            }
        }

        return dayReports;
    }

    private Date findEndedDate(DayReport _dayReport) {
        // apparently, we sometimes miss an ended date
        // for the heck of it - just find all dates
        // and choose the most occurring one...
        List<Date> dates = new ArrayList<>();
        for (DayReportEntry dayReportEntry : _dayReport.getDayReportEntries()) {
            Date reported = truncateDate(dayReportEntry.getReported(), Calendar.DAY_OF_MONTH);
            Date started = truncateDate(dayReportEntry.getStarted(), Calendar.DAY_OF_MONTH);
            Date ended = truncateDate(dayReportEntry.getEnded(), Calendar.DAY_OF_MONTH);
            addNoneNullToMap(dates, reported, started, ended);
        }

        Date mostUsed = dates.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Comparator.comparing(Entry::getValue))
                .get()
                .getKey();

        return mostUsed;
    }

    private void addNoneNullToMap(@Nonnull List<Date> _map, @Nullable Date... _dates) {
        for (Date date : _dates) {
            if(date != null) {
                _map.add(date);
            }
        }
    }

    @Nullable
    private Date truncateDate(@Nullable Date _date, int _field) {
        return (_date != null) ? DateUtils.truncate(_date, _field) : _date;
    }

    private DayReportEntry parseDayReportEntry(Element _reportEntry, DayReport _dayReport) throws java.text.ParseException {

        DayReportEntry dayReportEntry = new DayReportEntry(_dayReport);
        try {
            List<TextNode> children = _reportEntry.textNodes();

            dayReportEntry.setRegion(_dayReport.getRegion());

            processType(children.get(0), dayReportEntry);
            processLocation(children.get(1), dayReportEntry);
            processReported(children.get(2), dayReportEntry);
            processOccurred(children.get(3), dayReportEntry);
            processDescription(children.get(4), dayReportEntry);
        } catch (Exception e) {
            String message = "Unable to process dayreport entry, probably unhandled format: " + _reportEntry;
            LOGGER.warn(message);
            System.out.printf(message);
            dayReportEntry = null;
        }

        return dayReportEntry;
    }

    private void processDescription(TextNode _textNode, DayReportEntry _dayReportEntry) {
        try {
            String value = _textNode.text();
            _dayReportEntry.setDescription(value);
        } catch (Exception _e) {
            LOGGER.warn("Exception occurred while processing description: " + _e.getMessage() + "[" + _textNode + "]", _e);
        }
    }

    private void processOccurred(TextNode _textNode, DayReportEntry _dayReportEntry) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            String unformattedOccured = _textNode.text();
            if (unformattedOccured.contains("-")) {
                String unformattedOccuredSubstringStart = unformattedOccured.substring(unformattedOccured.indexOf(":") + 1, unformattedOccured.indexOf("-"));
                Date started = sdf.parse(unformattedOccuredSubstringStart.trim());

                String unformattedOccuredSubstringEnd = unformattedOccured.substring(unformattedOccured.indexOf("-") + 1);
                Date ended = sdf.parse(unformattedOccuredSubstringEnd.trim());

                _dayReportEntry.setStarted(started);
                _dayReportEntry.setEnded(ended);
            } else {
                String unformattedOccuredSubstringStart = unformattedOccured.substring(unformattedOccured.indexOf(":") + 1);
                Date started = sdf.parse(unformattedOccuredSubstringStart.trim());
                _dayReportEntry.setStarted(started);
            }
        } catch (Exception _e) {
            LOGGER.warn("Exception occurred while processing occurred: " + _e.getMessage() + "[" + _textNode + "]", _e);
        }
    }

    private void processReported(TextNode _textNode, DayReportEntry _dayReportEntry) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            String unformattedReported = _textNode.text();
            String unformattedReportedSubstring = unformattedReported.substring(unformattedReported.indexOf(":") + 1);
            Date reported = sdf.parse(unformattedReportedSubstring.trim());

            _dayReportEntry.setReported(reported);
        } catch (Exception _e) {
            LOGGER.warn("Exception occurred while processing reported: " + _e.getMessage() + "[" + _textNode + "]", _e);
        }
    }

    private void processLocation(TextNode _textNode, DayReportEntry _dayReportEntry) {
        try {
            String[] locationTokens = _textNode.text().split(" ");

            String zip = locationTokens[0].trim();
            _dayReportEntry.setZipCode(zip);

            String city = locationTokens[1].trim();
            _dayReportEntry.setCity(city);

            if (locationTokens.length > 3) {
                String location = locationTokens[3].trim();
                _dayReportEntry.setLocation(location);
            }
        } catch (Exception _e) {
            LOGGER.warn("Exception occurred while processing location: " + _e.getMessage() + "[" + _textNode + "]", _e);
        }
    }

    private void processType(TextNode _textNode, DayReportEntry _dayReportEntry) {
        try {
            String value = _textNode.text();
            _dayReportEntry.setType(value);
        } catch (Exception _e) {
            LOGGER.warn("Exception occurred while processing type: " + _e.getMessage() + "[" + _textNode + "]", _e);
        }
    }

    private void pause(long _ms) {
        try {
            Thread.sleep(_ms);
        } catch (InterruptedException inte) {
            /* ignored */
        }
    }

    private void logWithBody(CloseableHttpResponse _response, String _message) {
        String body = "";
        try {
            HttpEntity entity = _response.getEntity();
            body = EntityUtils.toString(entity);
        } catch (Exception _e) {
            /* ignored */
        }
        throw new RuntimeException(_message + ". StatusLine: " + _response.getStatusLine() + ", Body: " + body);
    }

}
