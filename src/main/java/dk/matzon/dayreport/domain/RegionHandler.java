package dk.matzon.dayreport.domain;

import dk.matzon.dayreport.domain.model.DayReport;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.Date;
import java.util.List;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public interface RegionHandler {
    String downloadReportOverviewPage(CloseableHttpClient _client, Date _from, Date _to) throws Exception;

    List<String> extractReportLinksFromHTML(String _reportOverviewPage) throws Exception;

    List<String> downloadHTMLReports(CloseableHttpClient _client, List<String> _reportLinks) throws Exception;

    List<DayReport> extractReportsFromHTML(List<String> _bodyReports) throws Exception;
}
