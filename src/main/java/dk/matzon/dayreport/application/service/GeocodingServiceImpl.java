package dk.matzon.dayreport.application.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import com.google.maps.model.LatLng;
import dk.matzon.dayreport.domain.GeocodingService;
import dk.matzon.dayreport.domain.model.DayReportEntry;
import dk.matzon.dayreport.infrastructure.persistence.DayReportEntryRepositoryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public class GeocodingServiceImpl implements GeocodingService {

    private final Logger LOGGER = LogManager.getLogger(GeocodingServiceImpl.class);

    private final GeoApiContext context;
    private final Properties properties;
    private DayReportEntryRepositoryImpl dayReportEntryRepository;
    private ScheduledExecutorService scheduledExecutorService;

    private ScheduledFuture<?> scheduledFuture;

    private ConcurrentHashMap<String, GeocodingResult> geocodingResults;

    public GeocodingServiceImpl(Properties _properties) {
        properties = _properties;
        context = new GeoApiContext().setApiKey(properties.getProperty("geocodingservice.apikey"))
                .setConnectTimeout(5, TimeUnit.SECONDS)
                .setReadTimeout(5, TimeUnit.SECONDS)
                .setWriteTimeout(5, TimeUnit.SECONDS);
    }

    public GeocodingServiceImpl(Properties _properties, DayReportEntryRepositoryImpl _dayReportEntryRepository, ScheduledExecutorService _scheduledExecutorService) {
        this(_properties);
        dayReportEntryRepository = _dayReportEntryRepository;
        scheduledExecutorService = _scheduledExecutorService;
        geocodingResults = new ConcurrentHashMap<>();
    }

    @Override
    public void initialize() {

        updateGeocodeResultCache();

        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                processEntriesMissingGeocodeInformation();
            }
        }, Long.parseLong(properties.getProperty("geocodingservice.datadelay")), Long.parseLong(properties.getProperty("geocodingservice.dataperiod")), TimeUnit.MINUTES);

    }

    private void updateGeocodeResultCache() {
        List<DayReportEntry> dayReportEntriesWithGeocode = dayReportEntryRepository.findDayReportEntriesWithGeocode();
        System.out.println("Updating geocode cache with " + dayReportEntriesWithGeocode.size() + " known entries");
        for (DayReportEntry dayReportEntry : dayReportEntriesWithGeocode) {
            String city = dayReportEntry.getCity();
            String zipCode = dayReportEntry.getZipCode();
            String location = dayReportEntry.getLocation();

            GeocodingResult geocodingResult = new GeocodingResult();
            geocodingResult.geometry = new Geometry();
            geocodingResult.geometry.location = new LatLng(dayReportEntry.getLatitude(), dayReportEntry.getLongtitude());

            updateExistingMatch(city, zipCode, location, geocodingResult);
        }

        List<DayReportEntry> all = dayReportEntryRepository.findAll();
        System.out.println("have " + all.size() + " dayreports in total");
    }

    public void processEntriesMissingGeocodeInformation() {
        List<DayReportEntry> entriesMissingGeoInformation = dayReportEntryRepository.findEntriesMissingGeoInformation();
        LOGGER.info("Processing " + entriesMissingGeoInformation.size() + " DayReportEntries with missing Geocode");

        for (DayReportEntry dayReportEntry : entriesMissingGeoInformation) {
            String city = dayReportEntry.getCity();
            String zipCode = dayReportEntry.getZipCode();
            String location = dayReportEntry.getLocation();

            try {
                GeocodingResult geocodingResult = existingMatch(city, zipCode, location);
                if (geocodingResult == null) {

                    // handle "empty" location
                    String geoLocation = location;
                    if ("-".equals(geoLocation)) {
                        geoLocation = "";
                    } else {
                        geoLocation = ", " + geoLocation;
                    }

                    String address = String.format("%s, %s%s", zipCode, city, geoLocation);
                    System.out.println("No result for address: " + address + ", performing lookup");

                    GeocodingResult[] geocodingResults = GeocodingApi.geocode(context, address).await();
                    if (geocodingResults != null && geocodingResults.length > 0) {
                        geocodingResult = geocodingResults[0];
                        updateExistingMatch(city, zipCode, location, geocodingResult);
                    } else {
                        System.out.println("No match for " + address);
                    }
                } else {
                    System.out.println("Already cached value for zipcode: " + zipCode + ", city: " + city + ", location: " + location);
                }

                if (geocodingResult != null) {
                    dayReportEntry.setLatitude(geocodingResult.geometry.location.lat);
                    dayReportEntry.setLongtitude(geocodingResult.geometry.location.lng);
                }

                Integer geocodeTries = dayReportEntry.getGeocodeTries();
                if (geocodeTries == null) {
                    geocodeTries = 0;
                }

                dayReportEntry.setGeocodeTries(geocodeTries + 1);
                dayReportEntryRepository.save(dayReportEntry);
            } catch (ApiException | InterruptedException | IOException _e) {
                String message = "Exception occured while processing Geocode request for zipcode: " + zipCode + "city: " + city + ", location: " + location;
                LOGGER.warn(message);
            }
        }
    }

    private void updateExistingMatch(String _city, String _zipCode, String _location, GeocodingResult _geocodingResult) {

        if (existingMatch(_city, _zipCode, _location) != null) {
            System.out.println("Not updating cache with existing: zipcode: " + _zipCode + ", city: " + _city + ", location: " + _location);
            return;
        }

        if (_location == null) {
            _location = "-";
        }

        String key = formatCacheKey(_zipCode, _city, _location);
        geocodingResults.put(key, _geocodingResult);

        String message = "Added GeocodingResult for zipcode: " + _zipCode + ", city: " + _city + ", location: " + _location + " lat: " + _geocodingResult.geometry.location.lat + ", lng: " + _geocodingResult.geometry.location.lng;
        LOGGER.info(message);
        System.out.println(message);
    }

    private GeocodingResult existingMatch(String _city, String _zipCode, String _location) {
        if (_location == null) {
            _location = "-";
        }
        String key = formatCacheKey(_zipCode, _city, _location);
        return geocodingResults.get(key);
    }

    private String formatCacheKey(String _city, String _zipCode, String _location) {
        return String.format("%s-%s-%s", _zipCode, _city, _location);
    }

    @Override
    public List<DayReportEntry> findFailingGeocodeEntries() {
        List<DayReportEntry> dayReportEntriesWithGeocodeErrorCount = dayReportEntryRepository.findDayReportEntriesWithGeocodeErrorCount(3);
        return dayReportEntriesWithGeocodeErrorCount;
    }

    public GeocodingResult[] lookup(String _zipCode, String _city, String _location) throws Exception {
        // handle "empty" location
        String geoLocation = _location;
        if ("-".equals(geoLocation)) {
            geoLocation = "";
        } else {
            geoLocation = ", " + geoLocation;
        }

        String address = String.format("%s, %s%s", _zipCode, _city, geoLocation);

        return GeocodingApi.geocode(context, address).await();
    }
}
