package co.luism.lukisoftiot.enterprise;

import co.luism.lukisoftiot.common.DiagnosticsPersistent;

import javax.persistence.*;

/**
 * Created by luis on 29.10.14.
 */
@Entity
@Table(catalog = "lukiiot")
public class AlarmValueHistoryInfo extends DiagnosticsPersistent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer id = 0;
    private Integer eventIndex;
    private Integer tagId;
    private String vehicleId;
    private Integer startTimeStamp;
    private Integer startTSMilliseconds;
    private Integer endTimeStamp;
    private Integer endTSMilliseconds;
    private long duration;
    private int numberOfEvents;
    private int eventCodeReference;
    private int statusInfo;
    private boolean ack = false;
    private String updateBy ="";
    @Transient
    private String guID;

    //map Many to One for tag Id
    @ManyToOne
    @JoinColumn(name="tagId", insertable=false, updatable=false, nullable=false)
    volatile private DataTag myTag = null;


    public AlarmValueHistoryInfo(){

    }

    public AlarmValueHistoryInfo(Integer tagId, String vehicleID, Integer eventIndex,
                                 Integer startTS, Integer startTSMilliseconds,
                                 Integer endTimeStamp, Integer endTSMilliseconds,
                        int numberOfEvents, int eventCodeReference, int statusInfo, DataTag dataTag){

        this.tagId = tagId;
        this.vehicleId = vehicleID;
        this.eventIndex = eventIndex;
        this.startTimeStamp = startTS;
        this.startTSMilliseconds = startTSMilliseconds;
        this.endTimeStamp = endTimeStamp;
        this.endTSMilliseconds = endTSMilliseconds;
        this.duration = (endTimeStamp*1000 + endTSMilliseconds) - (startTS*1000 + startTSMilliseconds);
        this.numberOfEvents = numberOfEvents;
        this.eventCodeReference = eventCodeReference;
        this.statusInfo = statusInfo;
        this.myTag = dataTag;

    }

    public AlarmValueHistoryInfo(AlarmValueHistoryInfo take) {
        this(take.getTagId(), take.getVehicleId(), take.getEventIndex(),
                take.getStartTimeStamp(), take.getStartTSMilliseconds(),
                take.getEndTimeStamp(), take.getEndTSMilliseconds(),
                take.getNumberOfEvents(),
                take.getEventCodeReference(), take.getStatusInfo(), take.getMyTag());
        this.id = take.getId();
        this.guID = take.getGuID();
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

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Integer getEventIndex() {
        return eventIndex;
    }

    public void setEventIndex(Integer eventIndex) {
        this.eventIndex = eventIndex;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getNumberOfEvents() {
        return numberOfEvents;
    }

    public void setNumberOfEvents(int numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    public int getEventCodeReference() {
        return eventCodeReference;
    }

    public void setEventCodeReference(int eventCodeReference) {
        this.eventCodeReference = eventCodeReference;
    }

    public int getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(int statusInfo) {
        this.statusInfo = statusInfo;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public Integer getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(Integer startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public Integer getStartTSMilliseconds() {
        return startTSMilliseconds;
    }

    public void setStartTSMilliseconds(Integer startTSMilliseconds) {
        this.startTSMilliseconds = startTSMilliseconds;
    }

    public Integer getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(Integer endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    public Integer getEndTSMilliseconds() {
        return endTSMilliseconds;
    }

    public void setEndTSMilliseconds(Integer endTSMilliseconds) {
        this.endTSMilliseconds = endTSMilliseconds;
    }

    public String getGuID() {
        return guID;
    }

    public void setGuID(String guID) {
        this.guID = guID;
    }

    public DataTag getMyTag() {
        return myTag;
    }

    public void setMyTag(DataTag myTag) {
        this.myTag = myTag;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }
}
