package dk.matzon.dayreport.domain.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Created by Brian Matzon <brian@matzon.dk>
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"date"}, name = "unique_day")})
public class DayReport implements Serializable {

    public static final Comparator<? super DayReport> DateComparator = Comparator.comparing(_o -> _o.date);
    
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "uuid", columnDefinition = "BINARY(16)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Region region;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @OneToMany(mappedBy = "dayReport", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<DayReportEntry> dayReportEntries = new ArrayList<>();

    public DayReport() {
    }

    public DayReport(Region _region, Date _date) {
        region = _region;
        date = _date;
    }

    public UUID getId() {
        return id;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region _region) {
        region = _region;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date _date) {
        date = _date;
    }

    public List<DayReportEntry> getDayReportEntries() {
        return dayReportEntries;
    }

    @Override
    public boolean equals(Object _o) {
        if (this == _o) return true;
        if (_o == null || getClass() != _o.getClass()) return false;
        DayReport dayReport = (DayReport) _o;
        return Objects.equals(id, dayReport.id) &&
                region == dayReport.region &&
                Objects.equals(date, dayReport.date) &&
                Objects.equals(dayReportEntries, dayReport.dayReportEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, region, date, dayReportEntries);
    }

    @Override
    public String toString() {
        return "DayReport{" +
                "id=" + id +
                ", region=" + region +
                ", date=" + date +
                ", dayReportEntries=" + dayReportEntries +
                '}';
    }
}
