package dk.matzon.dayreport.application.service.handlers;

import dk.matzon.dayreport.domain.model.Region;

import java.util.Properties;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public class NordSjaellandRegionHandler extends BaseRegionHandler {
    public NordSjaellandRegionHandler(Properties _properties) {
        super(Region.NORDSJAELLAND, _properties);
    }
}
