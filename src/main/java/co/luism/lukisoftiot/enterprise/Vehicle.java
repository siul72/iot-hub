package co.luism.lukisoftiot.enterprise;

import co.luism.lukisoftiot.common.DateTimeUtils;
import co.luism.lukisoftiot.datacollector.common.DataCollectorFrameSender;
import co.luism.lukisoftiot.datacollector.DataCollectorSocket;
import co.luism.lukisoftiot.datacollector.DataCollectorTimer;
import co.luism.lukisoftiot.datacollector.messages.DCLifeSign;
import co.luism.lukisoftiot.datacollector.messages.DCLifeSignParamEnum;
import co.luism.lukisoftiot.common.datatypes.UnsignedInteger;
import co.luism.lukisoftiot.interfaces.IVehicleEventHandler;
import co.luism.lukisoftiot.webapputils.WebAppUtils;
import co.luism.lukisoftiot.webapputils.WebManagerFacade;
import co.luism.lukisoftiot.common.*;
import org.apache.log4j.Logger;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

@XmlRootElement(name = "Vehicle")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(catalog = "lukiiot")
public class Vehicle extends DiagnosticsPersistent implements Comparable{
    @XmlTransient
    @Transient
    private static final long serialVersionUID = 1L;
    @XmlTransient
    @Transient
    private static final Logger LOG = Logger.getLogger(Vehicle.class);

    @Id
    @Column(nullable = false, length = 32, unique=true)
    private String vehicleId = "";

    @XmlTransient
    @ManyToOne
    @JoinColumn(name="fleetId", insertable=false, updatable=false, nullable=false)
    private Fleet myFleet = null;
    private Integer fleetId;
    @Column(nullable = false, length = 32)
	private String vehicleType = "NO_TYPE";
    @Column(nullable = false, length = 16)
    private String vehicleNumber = "";
    @Column(nullable = false, length = 20)
    private String  smsNumber = "";
    @Column(nullable = false, length = 16)
    private String protocolVersion = "1.00.00";
    @Column(nullable = false)
    private Integer timeZone = 0;
    @Column(nullable = false, length = 8)
    private String countryCode = "CH";
    @Column(nullable = false)
    private boolean  daylightSavingTime = false;
    @Column(nullable = false)
    private Boolean enabled = true;
    @XmlTransient
    @Column
    private String updateBy ="";
    @XmlTransient
    @Transient
    private VehicleStatusEnum status = VehicleStatusEnum.ST_OFFLINE;
    @XmlTransient
    @Transient
    private long statusUpdateTime;
    @XmlTransient
    @Transient
    private UnsignedInteger sessionCount = new UnsignedInteger(0);
    @XmlTransient
    @Transient
    private DataCollectorTimer suspendTimer;
    //Map TagType, TagId, TagValue
    @XmlTransient
    @Transient
    private final Map<EventTypeEnum, Map<Integer, ? super TagValue>> mapSnapshotTagValuesSourceId = new HashMap<>();
    @XmlTransient
    @Transient
    private final List<Integer> snapShotTagList = new ArrayList<>();

    @XmlTransient
    @Transient
    private SessionStatusEnum sessionStatus = SessionStatusEnum.SS_ST_NONE;

    @XmlTransient
    @Transient
    private VehicleSyncStatusEnum syncStatus = VehicleSyncStatusEnum.SYNC_STATUS_NONE;

    @XmlTransient
    @Transient
    private final Set<IVehicleEventHandler> listeners = new HashSet<>();

    @Transient
    private int configurationId = 0;

    public Vehicle(String vehicleId){
        this.vehicleId = vehicleId;

    }

    public Vehicle(){

    }

    public Fleet getMyFleet() {
        return myFleet;
    }
    public void setMyFleet(Fleet fleetId) {
        this.myFleet = fleetId;
        this.configurationId = this.myFleet.getConfigurationId();
    }
    public String getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(String vehicleID) {
		this.vehicleId = vehicleID;
	}
    public String getVehicleType() {
        return vehicleType;
    }
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
    public String getVehicleNumber() {
        return vehicleNumber;
    }
    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
    public String getSmsNumber() {
		return smsNumber;
	}
    public void setSmsNumber(String smsNumber) {
        this.smsNumber = smsNumber;
    }
    public String getProtocolVersion() {
        return protocolVersion;
    }
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
    public Integer getTimeZone() {
        return timeZone;
    }
    public void setTimeZone(Integer timeZone) {
        this.timeZone = timeZone;
    }
    public String getCountryCode() {
        return countryCode;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    public boolean isDaylightSavingTime() {
        return daylightSavingTime;
    }
    public void setDaylightSavingTime(boolean daylightSavingTime) {
        this.daylightSavingTime = daylightSavingTime;
    }
    public Boolean getEnabled() {
        return enabled;
    }
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    public Integer getFleetId() {
        return fleetId;
    }
    public void setFleetId(Integer fleetId) {
        this.fleetId = fleetId;
    }
    public VehicleStatusEnum getStatus() {
        return status;
    }
    public void setStatus(VehicleStatusEnum status) {

        switch (this.status){
            case ST_SUSPENDED:
                switch (status){
                    case ST_OFFLINE:
                    case ST_ONLINE:
                        if(this.suspendTimer != null) {
                            this.suspendTimer.cancelTimer();
                            this.suspendTimer=null;
                        }
                        break;

                }
            break;
            case ST_ONLINE:
                switch (status){
                    case ST_OFFLINE:
                        if(this.syncStatus != VehicleSyncStatusEnum.SYNC_STATUS_RUNNING){

                            //mark snapshot alarms as not fresh
                            markSnapShotAlarmsNotFresh();
                        }

                    break;
                }
            break;


        }

        this.status = status;


        switch(this.status){
            case ST_OFFLINE:
                this.sessionCount.setValue(0);
            break;
            case ST_ONLINE:
                sendConfigureProcessData();
            break;

        }

        this.setStatusUpdateTime(DateTimeUtils.getCurrentTimeStamp().getTime());
        WebAppUtils.getInstance().sendStatusUpdateEvent(this);
        DataTag tag = DCLifeSign.getInstance().get(DCLifeSignParamEnum.TRAIN_STATUS.ordinal());

        putSnapShotValue(EventTypeEnum.TAG_DATA_TYPE_SYSTEM, tag, DateTimeUtils.getCurrentTimeStampSeconds(),
                DiagnosticsConfig.DC_DEFAULT_MS_VALUE, this.status.getValue());

        LOG.debug(String.format("new status %s for vehicle %s", this.status.name(), this.vehicleId));

    }

    private void markSnapShotAlarmsNotFresh() {

        this.syncStatus = VehicleSyncStatusEnum.SYNC_STATUS_DB_TO_VEHICLE_OK;

        Map<Integer, ? super TagValue> myMap = this.mapSnapshotTagValuesSourceId.get(EventTypeEnum.TAG_DATA_TYPE_EVENT);

        if(myMap == null){
            LOG.warn("map is null");
            return;
        }

        for(Object obj : myMap.values()){
            if(obj instanceof SnapShotAlarmTagValue){
                SnapShotAlarmTagValue snapShotAlarmTagValue = (SnapShotAlarmTagValue)obj;
                snapShotAlarmTagValue.setAlarmSyncStatus(AlarmSyncStatus.ALARM_SYNC_OLD);

            }
        }

    }

    public String getUpdateBy() {
        return updateBy;
    }
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }
    public void setStatusUpdateTime(long statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }
    public long getStatusUpdateTime() {
        return statusUpdateTime;
    }
    public VehicleSyncStatusEnum getSyncStatus() {
        return syncStatus;
    }
    public void setSyncStatus(VehicleSyncStatusEnum syncStatus) {
        this.syncStatus = syncStatus;
    }
    public SessionStatusEnum getSessionStatus(){
        return sessionStatus;
    }

    public int getSessionCount(){
        return sessionCount.getValue();
    }

    public void incrementSessionCount(){

        int v = sessionCount.getValue();

        if(v == 0){
            sendStartProcessData();
        }

        sessionCount.setValue(++v);
    }

    public void decrementSessionCount(){

        int v = sessionCount.getValue();

        if(v == 1){
            sendStopProcessData();
        }

        if(v> 0) {
            sessionCount.setValue(--v);
        }
    }

    private void sendStartProcessData() {

        Runnable runnable = new Runnable() {
            public void run() {

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    LOG.error(e);
                    return;
                }

                String connectionGUID = DataCollectorSocket.getInstance().getGUID(vehicleId);

                if(connectionGUID == null){
                    sessionStatus = SessionStatusEnum.SS_ST_PENDING_START_PROCESS_DATA;
                    LOG.error("pending start process data - connection is null");
                    return;
                }
                int updateInterval = DiagnosticsConfig.PROCESS_DATA_UPDATE_INTERVAL;
                LOG.debug(String.format("START PROCESS DATA %s", vehicleId));
                if(!DataCollectorFrameSender.getInstance().
                        sendStartProcessData(connectionGUID, String.valueOf(updateInterval))){
                    sessionStatus = SessionStatusEnum.SS_ST_PENDING_START_PROCESS_DATA;
                    LOG.error("pending start process data - fail to send");
                } else {
                    sessionStatus = SessionStatusEnum.SS_ST_STARTED_PROCESS_DATA;
                }

            }

        };

        new Thread(runnable).start();

    }

    private void sendConfigureProcessData() {

        Runnable runnable = new Runnable() {
            public void run() {

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    LOG.error(e);
                    return;
                }

                String connectionGUID = DataCollectorSocket.getInstance().getGUID(vehicleId);

                if(connectionGUID == null){

                    LOG.error("connection is null");
                    return;
                }


                List<Integer> myOfflineList = new ArrayList<>();
                List<Integer> myOnlineList = new ArrayList<>();

                for(DataTag t : WebManagerFacade.getInstance().getAllTags()){

                    if(t.getType().equals(EventTypeEnum.TAG_DATA_TYPE_PD.getValue())){

                        for(DataScanCollector dataScanCollector : getMyFleet().getDataScannerList()){

                            if(t.getDataScanCollectorId() != null){
                                if(t.getDataScanCollectorId().equals(dataScanCollector.getDataScanCollectorId())){
                                    myOfflineList.add(t.getSourceTagId());
                                }
                            }

                        }


                        if(!t.getProcess().equals( ProcessEnum.PROCESS_NONE.getValue()) ){
                            myOnlineList.add(t.getSourceTagId());
                        }

                    }


                }

                LOG.debug("SEND CONFIG PROCESS DATA TO:" + vehicleId);

                if(!DataCollectorFrameSender.getInstance().sendConfigureProcessData(connectionGUID, myOfflineList, myOnlineList)){

                    LOG.error("not able to send configure");
                }

            }

        };

        new Thread(runnable).start();

    }


    private void sendStopProcessData() {
        String connectionGUID = DataCollectorSocket.getInstance().getGUID(this.vehicleId);
        if(connectionGUID == null){
            sessionStatus = SessionStatusEnum.SS_ST_PENDING_STOP_PROCESS_DATA;
            LOG.error("pending stop process data - connection is null");
            return;
        }
        LOG.debug(String.format("send process data stop for vehicle %s", this.getVehicleId()));
        if(!DataCollectorFrameSender.getInstance().sendStopProcessData(connectionGUID)){
            sessionStatus = SessionStatusEnum.SS_ST_PENDING_START_PROCESS_DATA;
            LOG.error("pending start process data - fail to send");
        } else {
            sessionStatus = SessionStatusEnum.SS_ST_STOPPED_PROCESS_DATA;
        }
    }

    public void setSuspendTimer(DataCollectorTimer suspendTimer) {
        this.suspendTimer = suspendTimer;
    }

    public void timerTimeOut() {
        if(this.suspendTimer != null){
            if(this.status == VehicleStatusEnum.ST_SUSPENDED){
                this.setStatus(VehicleStatusEnum.ST_OFFLINE);
            }
            this.suspendTimer = null;
        }
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Vehicle){
            if(this.vehicleId.equals(((Vehicle)o).getVehicleId())){
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 7 * hash + this.vehicleId.hashCode();
        return hash;

    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof Vehicle){

            int cmp = this.vehicleType.compareTo(((Vehicle)o).getVehicleType());

            if(cmp == 0){
                return this.vehicleNumber.compareTo(((Vehicle)o).getVehicleNumber());
            } else {
                return cmp;
            }
        }

        return 0;
    }

    public void putSnapShotValue(EventTypeEnum tagDataType, DataTag tag, Integer timeStamp, Integer tsMilliseconds, long value) {

        createUpdateSnapshotValuesIds(tagDataType, tag.getTagId(), tag.getScale(), timeStamp, tsMilliseconds, value, true);
    }

    public void putSnapShotValue(EventTypeEnum tagDataTypeEvent, SnapShotAlarmTagValue tagValue, boolean fromVehicle) {
        createUpdateSnapshotValuesIds(tagDataTypeEvent, tagValue.getTagId(), tagValue.getScale(),
                tagValue.getTimeStamp(), tagValue.getMilliSeconds(), tagValue.getValue(), fromVehicle);

    }

    private void createUpdateSnapshotValuesIds(EventTypeEnum tagDataType, Integer tagId,
                                               double scale, Integer timeStamp, Integer tsMilliseconds, long value, boolean fromVehicle) {

        TagValue tv;
        Map<Integer, ? super TagValue> myMap = this.mapSnapshotTagValuesSourceId.get(tagDataType);

        if(myMap == null) {
            myMap = new HashMap<>();
            this.mapSnapshotTagValuesSourceId.put(tagDataType, myMap);
        }

        if(myMap.containsKey(tagId)){
            tv = (TagValue)myMap.get(tagId);
            if(tv.getTimeStamp() >= timeStamp){
                LOG.error(String.format("the new timestamp is older or same then the snapshot %d", tagId));
                LOG.error(String.format("New:Old %d:%d", timeStamp, tv.getTimeStamp()));
                return;
            }

            tv.setTimeStamp(timeStamp);
            tv.setAck(false);
            tv.setValue(value);
            return;
        }

        Map <String, Object> myRestrictions = new HashMap<>();

        switch (tagDataType){

            case TAG_DATA_TYPE_EVENT:
                myRestrictions.put("vehicleId", this.vehicleId);
                myRestrictions.put("tagId", tagId);
                tv = read(SnapShotAlarmTagValue.class, myRestrictions);
                if(tv == null){
                    tv = new SnapShotAlarmTagValue(this.vehicleId, tagId, timeStamp, tsMilliseconds, value, scale, fromVehicle);
                    tv.create();
                } else {
                    tv.setTimeStamp(timeStamp);
                    tv.setValue(value);
                    tv.setAck(false);
                    tv.update();
                }
            break;

            default:
               tv = new GenericTagValue(tagDataType, this.vehicleId, tagId, timeStamp,tsMilliseconds, value, scale);
                this.snapShotTagList.add(tagId);
            break;
        }

        myMap.put(tagId, tv);

    }


    public <T> T  getSnapShotValue(Class<? extends T> type, EventTypeEnum tagDataType, DataTag dataTag) {
        Map<Integer, ? super TagValue> myNameMap = this.mapSnapshotTagValuesSourceId.get(tagDataType);
        if(myNameMap == null){
            return null;
        }

        if(dataTag == null){
            return null;
        }

        Integer id;

        switch (tagDataType){
            case TAG_DATA_TYPE_EVENT:
                   id= dataTag.getSourceTagId();
                break;
            case TAG_DATA_TYPE_PD:
            case TAG_DATA_TYPE_SYSTEM:
                id = dataTag.getTagId();
            break;
            default:
                return null;

        }

        if(id == null){
            return null;
        }

        return  type.cast(myNameMap.get(id));

    }

    public <T> T  getSnapShotValue(Class<? extends T> type, EventTypeEnum tagDataTypeSystem, String tagName) {
        Map<Integer, ? super TagValue> myNameMap = this.mapSnapshotTagValuesSourceId.get(tagDataTypeSystem);
        if(myNameMap == null){
            return null;
        }

        DataTag dataTag = WebManagerFacade.getInstance().getTagByName(tagDataTypeSystem, 0,tagName);
        Integer id;

        if(dataTag == null){
            return null;
        }

        switch (tagDataTypeSystem){
            case TAG_DATA_TYPE_SYSTEM:

                id = dataTag.getTagId();
                break;
            default:
                LOG.warn("not implemented");
                return null;

        }

        if(id == null){
            return null;
        }

        return  type.cast(myNameMap.get(id));

    }

    public <T> T  getSnapShotValue(Class<? extends T> type, EventTypeEnum tagDataTypeSystem, Integer tagSourceId) {

        Map<Integer, ? super TagValue> myNameMap = this.mapSnapshotTagValuesSourceId.get(tagDataTypeSystem);
        if(myNameMap == null){
            return null;
        }

        return  type.cast(myNameMap.get(tagSourceId));

    }

    public GenericTagValue getSnapShotValue(int tagId) {

        for(Map m : this.mapSnapshotTagValuesSourceId.values()){
            Object t = m.get(tagId);

            if(t instanceof GenericTagValue){

                if(((GenericTagValue) t).getTagId() == tagId){
                    return (GenericTagValue) t;
                }

            }

        }

        return null;
    }

    public List<SnapShotAlarmTagValue> getActiveAlarms(){

        List<SnapShotAlarmTagValue> myReturnList = new ArrayList<>();
        List myList =  getActiveValues(SnapShotAlarmTagValue.class);
        for(Object obj : myList){
            if(obj instanceof SnapShotAlarmTagValue){
                if(((SnapShotAlarmTagValue) obj).isAck()){
                    continue;
                }
                myReturnList.add((SnapShotAlarmTagValue)obj);
            }

        }

        return myReturnList;

    }

    public List<SnapShotGenericValue> getProcessDataValues(){

        List<SnapShotGenericValue> myList = new ArrayList<>();
        Map<Integer, ? super TagValue> myNameMap = this.mapSnapshotTagValuesSourceId.get(EventTypeEnum.TAG_DATA_TYPE_PD);
        if(myNameMap == null){
            return myList;
        }

        for(Object v : myNameMap.values()){

            if(v instanceof TagValue){


                if(((TagValue)v).isAck()){
                    continue;
                }

                myList.add(SnapShotGenericValue.class.cast(v));
            }


        }

        return myList;

    }

    public void clearAlarmList() {

        Map<Integer, ? super TagValue> myNameMap = this.mapSnapshotTagValuesSourceId.get(EventTypeEnum.TAG_DATA_TYPE_EVENT);
        if(myNameMap!= null){
            myNameMap.clear();
            LOG.info("the alarm list was cleared");
        }

    }

    private <T> List<T> getActiveValues(Class<T> clazz){

        List<T> myList = new ArrayList<>();
        Map<Integer, ? super TagValue> myNameMap = this.mapSnapshotTagValuesSourceId.get(EventTypeEnum.TAG_DATA_TYPE_EVENT);
        if(myNameMap == null){
            return myList;
        }

        for(Object v : myNameMap.values()){

           if(v instanceof TagValue){


                if(((TagValue)v).isAck()){
                    continue;
                }

                if(((TagValue)v).getValue() == 0){
                    continue;
                }

                myList.add(clazz.cast(v));
            }


        }

        return myList;


    }


    public SnapShotAlarmTagValue getAlarmTag(Integer idx) {

        Map myMap = this.mapSnapshotTagValuesSourceId.get(EventTypeEnum.TAG_DATA_TYPE_EVENT);

        if(myMap == null){
            LOG.error("snapshot map for TAG_DATA_TYPE_EVENT not ready");
            return null;
        }

        Object myObj = myMap.get(idx);

        if(myObj instanceof SnapShotAlarmTagValue){
            return (SnapShotAlarmTagValue)myObj;
        }

        return null;
    }

    public void addListener(IVehicleEventHandler IVehicleEventHandler){
        this.listeners.add(IVehicleEventHandler);
        fireEvent(new VehicleEvent(this));
    }

    public void removeListener(IVehicleEventHandler IVehicleEventHandler){
        this.listeners.remove(IVehicleEventHandler);
    }

    private void fireEvent(final VehicleEvent vehicleEvent) {

        for (final IVehicleEventHandler IVehicleEventHandler : listeners) {
            Runnable runnable = new Runnable() {
                public void run() {

                    IVehicleEventHandler.handleVehicleEvent(vehicleEvent);

                }

            };
            new Thread(runnable).start();
        }
    }

    public void fireNoMoreData(){
        if(this.syncStatus != VehicleSyncStatusEnum.SYNC_STATUS_ALL_SYNC){
           fireEvent(new VehicleEvent(this));
        }
    }

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
    }

    public Collection<Integer> getSnapShotTagList() {
        return snapShotTagList;
    }



}
  
