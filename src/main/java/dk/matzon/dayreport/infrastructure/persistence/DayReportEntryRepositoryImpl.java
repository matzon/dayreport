package dk.matzon.dayreport.infrastructure.persistence;

import dk.matzon.dayreport.domain.model.DayReport;
import org.hibernate.SessionFactory;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public class DayReportEntryRepositoryImpl extends DayReportAbstractRepositoryImpl<DayReport> {
    public DayReportEntryRepositoryImpl(SessionFactory _sessionFactory) {
        super(_sessionFactory, DayReport.class);
    }
}
