package dk.matzon.dayreport.application.service.handlers;

import dk.matzon.dayreport.domain.model.Region;

import java.util.Properties;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public class FynRegionHandler extends BaseRegionHandler {
    public FynRegionHandler(Properties _properties) {
        super(Region.FYN, _properties);
    }
}
