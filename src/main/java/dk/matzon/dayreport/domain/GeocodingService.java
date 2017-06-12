package dk.matzon.dayreport.domain;

import dk.matzon.dayreport.domain.model.DayReportEntry;

import java.util.List;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public interface GeocodingService {
    void initialize();

    List<DayReportEntry> findFailingGeocodeEntries();

    void processEntriesMissingGeocodeInformation();
}
