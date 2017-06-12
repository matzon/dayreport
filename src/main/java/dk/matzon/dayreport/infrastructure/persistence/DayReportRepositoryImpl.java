package dk.matzon.dayreport.infrastructure.persistence;

import dk.matzon.dayreport.domain.model.DayReport;
import org.hibernate.SessionFactory;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public class DayReportRepositoryImpl extends DayReportAbstractRepositoryImpl<DayReport> {
    public DayReportRepositoryImpl(SessionFactory _sessionFactory) {
        super(_sessionFactory, DayReport.class);
    }
}
