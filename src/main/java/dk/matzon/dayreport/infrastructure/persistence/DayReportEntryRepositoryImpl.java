package dk.matzon.dayreport.infrastructure.persistence;

import dk.matzon.dayreport.domain.model.DayReportEntry;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Collections;
import java.util.List;

/**
 * Created by Brian Matzon <brian@matzon.dk>.
 */
public class DayReportEntryRepositoryImpl extends DayReportAbstractRepositoryImpl<DayReportEntry> {
    public DayReportEntryRepositoryImpl(SessionFactory _sessionFactory) {
        super(_sessionFactory, DayReportEntry.class);
    }

    public List<DayReportEntry> findEntriesMissingGeoInformation() {
        List<DayReportEntry> result = withTransactionableSession((_session, _transaction) -> {
            Query query = _session.createQuery("from " + DayReportEntry.class.getName() + " WHERE latitude IS NULL and longtitude IS NULL AND (geocodeTries IS NULL OR geocodeTries <= 3)");
            List list = query.list();
            _transaction.commit();
            return list;
        });

        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    public List<DayReportEntry> findDayReportEntriesWithGeocodeErrorCount(int _errorCount) {
        List<DayReportEntry> result = withTransactionableSession((_session, _transaction) -> {
            Query query = _session.createQuery("from " + DayReportEntry.class.getName() + " WHERE geocodeTries >= :errorCount");
            query.setParameter("errorCount", _errorCount);
            List list = query.list();
            _transaction.commit();
            return list;
        });

        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

    public List<DayReportEntry> findDayReportEntriesWithGeocode() {
        List<DayReportEntry> result = withTransactionableSession((_session, _transaction) -> {
            Query query = _session.createQuery("from " + DayReportEntry.class.getName() + " WHERE latitude IS NOT NULL and longtitude IS NOT NULL");
            List list = query.list();
            _transaction.commit();
            return list;
        });

        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }
}
