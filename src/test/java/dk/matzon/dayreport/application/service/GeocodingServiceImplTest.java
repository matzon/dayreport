package dk.matzon.dayreport.application.service;

import com.google.maps.model.GeocodingResult;
import org.junit.Test;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public class GeocodingServiceImplTest {

    @Test
    public void testSingleLookup() throws Exception {
        Properties properties = new Properties();
        InputStream configInputStream = GeocodingServiceImplTest.class.getResourceAsStream("/config.properties");
        properties.load(configInputStream);

        GeocodingServiceImpl geocodingService = new GeocodingServiceImpl(properties);  
                GeocodingResult[] lookup = geocodingService.lookup("5471", "Søndersø", "Omfartsvejen");

        if (lookup != null) {
            System.out.println("Found " + lookup.length + " entries");
            for (GeocodingResult geocodingResult : lookup) {
                System.out.println("lat: " + geocodingResult.geometry.location.lat);
                System.out.println("lng: " + geocodingResult.geometry.location.lng);
            }
        } else {
            System.out.println("Did not find a match");
        }
    }
}