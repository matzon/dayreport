package dk.matzon.dayreport.domain;

import dk.matzon.dayreport.domain.model.Region;

import java.util.Date;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public interface DataGatherer {
    void initialize();

    void shutdown();

    boolean isRunning();

    long getTimeForNextJob();

    void downloadData(Date _from, Date _to, Region _region);

}
