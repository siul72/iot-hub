package co.luism.diagnostics.enterprise;

import co.luism.diagnostics.common.DiagnosticsPersistent;

import javax.persistence.*;

/**
 * Created by luis on 28.11.14.
 */
@Entity
@Table(catalog = "ondiagnose")
public class AlarmEnvironmentData extends DiagnosticsPersistent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer id = 0;
    private String updateBy = "";
    private Integer alarmTagHistoryInfoId;
    private Integer eventIndex;
    private Integer categoryIndex;
    private Integer timeStampSeconds;
    private Integer timeStampMilliSeconds = 0;
    private String value;

    @ManyToOne
    @JoinColumn(name="alarmTagHistoryInfoId", insertable=false, updatable=false, nullable=false)
    private AlarmValueHistoryInfo myAlarmValueHistoryInfo = null;

    public AlarmEnvironmentData(){

    }

    public AlarmEnvironmentData(Integer id, Integer eventIndex, Integer categoryIndex, int timeStampSeconds, int timeStampMilliSeconds, String value) {
        this.alarmTagHistoryInfoId = id;
        this.eventIndex = eventIndex;
        this.categoryIndex = categoryIndex;
        this.timeStampSeconds = timeStampSeconds;
        this.timeStampMilliSeconds = timeStampMilliSeconds;
        this.value = value;
    }

    @Override
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public Integer getAlarmTagHistoryInfoId() {
        return alarmTagHistoryInfoId;
    }

    public void setAlarmTagHistoryInfoId(Integer alarmTagHistoryInfoId) {
        this.alarmTagHistoryInfoId = alarmTagHistoryInfoId;
    }

    public Integer getEventIndex() {
        return eventIndex;
    }

    public void setEventIndex(Integer eventIndex) {
        this.eventIndex = eventIndex;
    }

    public Integer getCategoryIndex() {
        return categoryIndex;
    }

    public void setCategoryIndex(Integer categoryIndex) {
        this.categoryIndex = categoryIndex;
    }

    public Integer getTimeStampSeconds() {
        return timeStampSeconds;
    }

    public void setTimeStampSeconds(Integer timeStampSeconds) {
        this.timeStampSeconds = timeStampSeconds;
    }

    public Integer getTimeStampMilliSeconds() {
        return timeStampMilliSeconds;
    }

    public void setTimeStampMilliSeconds(Integer timeStampMilliSeconds) {
        this.timeStampMilliSeconds = timeStampMilliSeconds;
    }

    public AlarmValueHistoryInfo getMyAlarmValueHistoryInfo() {
        return myAlarmValueHistoryInfo;
    }

    public void setMyAlarmValueHistoryInfo(AlarmValueHistoryInfo myAlarmValueHistoryInfo) {
        this.myAlarmValueHistoryInfo = myAlarmValueHistoryInfo;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
