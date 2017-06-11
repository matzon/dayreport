package dk.matzon.dayreport.application.service.handlers;

import dk.matzon.dayreport.domain.RegionHandler;
import dk.matzon.dayreport.domain.model.Region;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Properties;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public class RegionHandlerFactory {

    @Nullable
    public static RegionHandler getHandlerForRegion(@Nonnull Region _region, @Nonnull Properties _properties) {
        switch (_region) {
            case FYN:
                return new FynRegionHandler(_properties);
            case NORDSJAELLAND:
                return new NordSjaellandRegionHandler(_properties);
            default:
                return null;
        }
    }
}
