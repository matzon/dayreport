package dk.matzon.dayreport.application.service.handlers;

import dk.matzon.dayreport.domain.model.DayReportEntry;
import dk.matzon.dayreport.domain.model.Region;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public class BaseRegionHandlerTest {

    @Test
    public void testBrokenLocationParsing() {
        String value = "5000 Odense C - Vestergade";
        BaseRegionHandler baseRegionHandler = new BaseRegionHandler(Region.FYN, null);
        DayReportEntry dayReportEntry = new DayReportEntry();
        baseRegionHandler.processLocation(value, dayReportEntry);

        assertEquals("5000", dayReportEntry.getZipCode());
        assertEquals("Odense", dayReportEntry.getCity());
        assertEquals("-", dayReportEntry.getLocation());
    }
}