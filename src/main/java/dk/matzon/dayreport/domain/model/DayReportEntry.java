package dk.matzon.dayreport.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by Brian Matzon <brian@matzon.dk>
 */
@Entity
@Table(name = "DAYREPORT_ENTRIES")
public class DayReportEntry implements Serializable {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "uuid", columnDefinition = "BINARY(16)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Region region;

    private String type;
    private String zipCode;
    private String city;
    private String location;

    @Temporal(TemporalType.TIMESTAMP)
    private Date reported;

    @Temporal(TemporalType.TIMESTAMP)
    private Date started;

    @Temporal(TemporalType.TIMESTAMP)
    private Date ended;

    @Column(length = 2048)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dayreport_id")
    private DayReport dayReport;

    public DayReportEntry() {
    }

    public DayReportEntry(Region _region, String _type, String _zipCode, String _city, String _location, Date _reported, Date _started, Date _ended, String _description, DayReport _dayReport) {
        region = _region;
        type = _type;
        zipCode = _zipCode;
        city = _city;
        location = _location;
        reported = _reported;
        started = _started;
        ended = _ended;
        description = _description;
        dayReport = _dayReport;
    }

    public DayReportEntry(DayReport _dayReport) {
        this.dayReport = _dayReport;
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

    public String getType() {
        return type;
    }

    public void setType(String _type) {
        type = _type;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String _zipCode) {
        zipCode = _zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String _city) {
        city = _city;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String _location) {
        location = _location;
    }

    public Date getReported() {
        return reported;
    }

    public void setReported(Date _reported) {
        reported = _reported;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date _started) {
        started = _started;
    }

    public Date getEnded() {
        return ended;
    }

    public void setEnded(Date _ended) {
        ended = _ended;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String _description) {
        description = _description;
    }

    @JsonIgnore
    public DayReport getDayReport() {
        return dayReport;
    }

    @Override
    public boolean equals(Object _o) {
        if (this == _o) return true;
        if (_o == null || getClass() != _o.getClass()) return false;
        DayReportEntry that = (DayReportEntry) _o;
        return Objects.equals(id, that.id) &&
                region == that.region &&
                Objects.equals(type, that.type) &&
                Objects.equals(zipCode, that.zipCode) &&
                Objects.equals(city, that.city) &&
                Objects.equals(location, that.location) &&
                Objects.equals(reported, that.reported) &&
                Objects.equals(started, that.started) &&
                Objects.equals(ended, that.ended) &&
                Objects.equals(description, that.description) &&
                Objects.equals(dayReport, that.dayReport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, region, type, zipCode, city, location, reported, started, ended, description, dayReport);
    }

    @Override
    public String toString() {
        return "DayReportEntry{" +
                "id=" + id +
                ", region=" + region +
                ", type='" + type + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", city='" + city + '\'' +
                ", location='" + location + '\'' +
                ", reported=" + reported +
                ", started=" + started +
                ", ended=" + ended +
                ", description='" + description + '\'' +
                '}';
    }
}
