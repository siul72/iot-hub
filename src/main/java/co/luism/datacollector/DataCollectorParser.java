package co.luism.datacollector;
 

import co.luism.datacollector.common.DCParserAckNackEvent;
import co.luism.datacollector.common.DCParserAckNackEventHandler;
import co.luism.datacollector.common.DataCollectorClient;
import co.luism.datacollector.common.DataCollectorFrameSender;
import co.luism.datacollector.messages.DCLifeSign;
import co.luism.datacollector.messages.DCLifeSignParamEnum;
import co.luism.diagnostics.common.*;
import co.luism.diagnostics.enterprise.*;
import co.luism.diagnostics.webmanager.WebManager;
import co.luism.diagnostics.webmanager.WebManagerFacade;
import org.apache.log4j.Logger;

import java.util.*;

final public class DataCollectorParser {

    private static DataCollectorParser instance = null;
	private static final Logger LOG = Logger.getLogger(DataCollectorParser.class);
    private Set<DCParserAckNackEventHandler> listeners = new HashSet<>();
	private DataCollectorParser() {
		LOG.info("Start");
	}

	static public DataCollectorParser getInstance() {

		if (instance == null) {
			instance = new DataCollectorParser();
		}

		return instance;
	}
	
	public boolean parse(String guId, String s){

        boolean ret = false;
		
		String[] frameValues = parseHeader(s);
		
		if(frameValues == null)
		{
			LOG.error(String.format("Invalid frame received %s", s));
			return false;
		}
		

        int x;
        try {
            //function index
            x = Integer.parseInt(frameValues[0]);
        }catch (NumberFormatException e){
            this.LOG.error("Error parsing function:" + e.getMessage());
            return false;
        }
		FunctionEnum myFunction = FunctionEnum.values()[x];
		switch(myFunction){

            case FC_ACK:
            case FC_NACK:
                parseAckNack(myFunction, guId,frameValues);
            break;

            case FC_EVENT_DATA:
                ret = parseEventData(guId, frameValues);
            break;
            case FC_HELLO:
                parseHello(guId, frameValues);
             break;
            case FC_LIFE_SIGN:
                parseLifeSign(guId, frameValues);
             break;
             case FC_PROCESS_DATA:
                 ret = parseProcessData(guId, frameValues);
             break;

             case FC_SUSPEND_CONNECTION:
                parseSuspendConnection(guId, frameValues);
             break;
            case FC_RESUME_CONNECTION:
                ret = parseResumeConnection(guId, frameValues);
            break;
            case FC_DIAGD_RESPONSE:
                //re-parseStringToBigDecimal to avoid bad split
                //frameValues = parseHeader(s, DiagnosticsConfig.MSG_DIAGD_SIZE);
                ret = parseDiagnosticsDeamonResponse(guId, frameValues);
            break;

            default:
            break;
			 
		}

        if(ret){
            DataCollectorClient cli = DataCollectorSocket.getInstance().getClientByGuid(guId);
            if(cli != null){
                cli.updateTimeOut();
            }

        }
		 
		return ret;
	}

    private void parseAckNack(FunctionEnum myFunction, String guId, String[] frameValues) {

        //check for witch message is the ack nack
        Integer ix;
        try{
            ix = Integer.parseInt(frameValues[DiagnosticsConfig.MSG_ACK_FUNCTION_POS]);
        } catch (NumberFormatException e){
            LOG.error(e);
            return;
        }
        FunctionEnum f = FunctionEnum.getFunction(ix);

        if(f == null){
            LOG.error("parseAckNack no valid function");
            return;
        }

        switch (f){
            case FC_START_VNC_SERVER:
                handleAckNackForStartVncServer(f, myFunction, guId);
            break;
            default:
                LOG.debug("default ack nack handling");
            break;
        }
    }



    private boolean parseDiagnosticsDeamonResponse(String guId, String[] frameValues) {
        boolean ret = false;

        DataCollectorFrameSender mySender = DataCollectorFrameSender.getInstance();
        if(frameValues.length <= DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS){
            LOG.error(String.format("frameValues.length=%d <= MSG_HEADER_SEQUENCE_NUMBER_POS=%d",
                    frameValues.length, DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS));
            return false;
        }
        String sequenceNumber = frameValues[DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS];

        if(frameValues.length <= DiagnosticsConfig.MSG_DIAGD_COMMAND_POS){
            LOG.error(String.format("frameValues.length=%d <= DiagnosticsConfig.MSG_DIAGD_COMMAND_POS=%d",
                    frameValues.length, DiagnosticsConfig.MSG_DIAGD_COMMAND_POS));
            return false;
        }
        String command = frameValues[DiagnosticsConfig.MSG_DIAGD_COMMAND_POS];
        DiagDFunctionEnum functionEnum =  DiagDFunctionEnum.getName(command);

        if(functionEnum == null){

            LOG.error(String.format("invalid function %s on parseDiagnosticsDeamonResponse", command));
            return false;
        }

        String valueData;
        switch (functionEnum){
            case FC_GET_NEXT_EVENT_RESPONSE:
                valueData = frameValues[DiagnosticsConfig.MSG_DIAGD_DATA_POS];
                LOG.debug(String.format("Received NEW ALARM %s", valueData));
                if(parseDiagnosticsDeamonNewEvent(guId, valueData)){
                    mySender.sendAckToDiagnosticsDeamon(guId, functionEnum.FC_GET_NEXT_EVENT_RESPONSE, sequenceNumber);
                    ret = true;
                }
             break;
            case FC_GET_ENV_DATA:

                LOG.debug(String.format("Received Environment Data Message"));
                //here we need to pass the complete frame
                if(parseDiagnosticsDeamonEnvironmentData(guId, frameValues)){
                    ret = true;
                }
            break;

        }

        return ret;
    }

    private boolean parseDiagnosticsDeamonEnvironmentData(String guId, String [] frameValues) {

        //check for EventIndex and BufferId
        Integer eventIndex, categoryIndex;


        try{
            eventIndex = Integer.parseInt(frameValues[DiagnosticsConfig.MSG_DIAGD_ENVDATA_INDEX_POS]);
        } catch (NumberFormatException e){
            LOG.error(e);
            return false;
        }

        try {
            categoryIndex = Integer.parseInt(frameValues[DiagnosticsConfig.MSG_DIAGD_ENVDATA_CATEGORY_INDEX_POS]);
        } catch (NumberFormatException e){
            LOG.error(e);
            return false;
        }

        AlarmEnvironmentDataRequester myAlarm = DataCollectorDiagnosticEventPuller.getInstance().findAlarmRequester(eventIndex, categoryIndex);

        if(myAlarm == null){
            LOG.error(String.format("AlarmEnvironmentDataRequester not found for Event %d and Category %d",
                    eventIndex, categoryIndex));
            return false;
        }

        //check for error codes
        String function = frameValues[DiagnosticsConfig.MSG_DIAGD_ENVDATA_STATUS_POS];
        DiagDFunctionEnum f = DiagDFunctionEnum.getName(function);

        if(f == null){
            LOG.error(String.format("invalid DiagDFunctionEnum for Event %d and Category %d",
                    eventIndex, categoryIndex));
            DataCollectorDiagnosticEventPuller.getInstance().removeSendEnvironmentData(myAlarm);
            return false;
        }

        switch (f){
            case FC_ENV_DATA_END:
            case FC_ENV_DATA_NO_DATA_FOUND:
                LOG.info(String.format("No more data for %d:%d (event index:category index)", eventIndex, categoryIndex));
                DataCollectorDiagnosticEventPuller.getInstance().removeSendEnvironmentData(myAlarm);
            return true;

            case FC_ENV_DATA_POINTER_ERROR:
                LOG.info(String.format("Error on pointer for %d:%d (event index:category index)", eventIndex, categoryIndex));
                DataCollectorDiagnosticEventPuller.getInstance().restartSendEnvironmentData(myAlarm);
            return true;

            case FC_ENV_DATA_DATA:
                //if not start with error then is a DATA buffer
                parseDiagnosticsDeamonEnvironmentDataData(myAlarm, frameValues);

                //get next
                DataCollectorDiagnosticEventPuller.getInstance().nextSendEnvironmentData(myAlarm);
            return true;
            default:
            break;
        }

        return false;
    }

    private void parseDiagnosticsDeamonEnvironmentDataData(AlarmEnvironmentDataRequester myAlarm, String[] frameValue) {


        int timeStampSeconds, timeStampMilliSeconds;

        try{
            timeStampSeconds = Integer.parseInt(frameValue[DiagnosticsConfig.MSG_DIAGD_ENVDATA_DATA_TS_POS]);
            timeStampMilliSeconds = Integer.parseInt(frameValue[DiagnosticsConfig.MSG_DIAGD_ENVDATA_DATA_TS_MS_POS]);
        }catch (NumberFormatException e){
            LOG.error(e);
            return;
        }

        String value = frameValue[DiagnosticsConfig.MSG_DIAGD_ENVDATA_DATA_DATA_CHUNK_POS];
        value = value.replace('>', ';');


        AlarmEnvironmentData environmentData = new AlarmEnvironmentData(myAlarm.getId(),myAlarm.getEventIndex(),
                myAlarm.getCategoryIndex(),
                timeStampSeconds, timeStampMilliSeconds, value);

        environmentData.create();
    }

    private boolean parseDiagnosticsDeamonNewEvent(String guId, String valueData) {

        //ev_index,ON_time_sec, ON_time_ms ,OFF_time_sec, OFF_time_ms,active_ev_count,ev_code,status_info
        //13,1426512756,536,1426512766,636,2,202,4

        Vehicle vehicle;

        try {
            vehicle = DataCollectorSocket.getInstance().findVehicleByGuId(guId);
        } catch (NullPointerException e){
            LOG.error("vehicle not found" + e);
            return false;
        }

        //replace < >
        valueData = valueData.replace(">", "");
        String [] dataValues = valueData.split(DiagnosticsConfig.DAIGD_EVENT_STRING_SPLITER);
        if(dataValues.length <= 0){
            LOG.error(String.format("DAIGD EVENT with wrong format, dataValues.length <= 0"));
            return false;
        }

        if(dataValues.length < DiagnosticsConfig.DIAGD_EVENT_SIZE){
            DiagDFunctionEnum df;

            try{
                df = DiagDFunctionEnum.getName(dataValues[0]);

                if(df == null){
                    LOG.error(String.format("is not function %s", dataValues[0]));

                } else {
                    //check if is a wrong response
                    switch (df){
                        case  FC_NO_MORE_EVENTS:
                            DataCollectorSocket.getInstance().setGetNewEventLow(guId, true);
                            vehicle.fireNoMoreData();
                        return true;
                    }
                }

            } catch (Exception e){
                LOG.error(e);
                return false;
            }
        }

        DataCollectorSocket.getInstance().setGetNewEventLow(guId, false);

        if(dataValues.length < DiagnosticsConfig.DIAGD_EVENT_SIZE){
            LOG.error(String.format("DAIGD EVENT with wrong size, expected %d, received %d",
                    DiagnosticsConfig.DIAGD_EVENT_SIZE, dataValues.length));
            return false;
        }

        //ev_index,ON_time_sec, ON_time_ms ,OFF_time_sec, OFF_time_ms,active_ev_count,ev_code,status_info

        Integer sourceTagId;
        try {
            sourceTagId= Integer.parseInt(dataValues[DiagnosticsConfig.DAIGD_EVENT_REF_POS]);
        } catch (NumberFormatException e){
            LOG.error("parseDiagnosticsDeamonNewEvent:Invalid format number for id or value");
            return false;
        }

        //find if event exists
        DataTag tag = WebManagerFacade.getInstance().getTagBySourceId(EventTypeEnum.TAG_DATA_TYPE_EVENT, vehicle.getConfigurationId(), sourceTagId);

        if(tag == null){
            LOG.warn(String.format("Tag not found for ev_code %d please review diagd settings", sourceTagId));
            //create a new one
            //tag = new DataTag(EventTypeEnum.TAG_DATA_TYPE_EVENT, vehicle.getConfigurationId(), sourceTagId, "dummy_" + sourceTagId);
            //WebManager.getInstance().add(tag);
            //tag.create();
            return false;
        }

        LOG.debug(String.format("new value for source tag %s", tag.getName()));

        Integer startTS, startTSms, endTS, endTSms;
        try{
            startTS = Integer.parseInt(dataValues[DiagnosticsConfig.DAIGD_EVENT_START_TS_POS]);
            startTSms = Integer.parseInt(dataValues[DiagnosticsConfig.DAIGD_EVENT_START_TS_MS_POS]);
            endTS = Integer.parseInt(dataValues[DiagnosticsConfig.DAIGD_EVENT_END_TS_POS]);
            endTSms = Integer.parseInt(dataValues[DiagnosticsConfig.DAIGD_EVENT_END_TS_MS_POS]);
        } catch (NumberFormatException e){

            LOG.error("invalid format for start time stamp");
            return false;
        }


        if(endTS == 0){
            LOG.debug(String.format("this is a new event"));
            createUpdateAlarm(vehicle, tag.getTagId(), startTS, startTSms, true, 1);
            updateSnapShot(vehicle, tag, startTS, startTSms, 1);
            return true;
        }

        LOG.debug(String.format("this is a closing event"));
        createUpdateAlarm(vehicle, tag.getTagId(), startTS, startTSms, true, 1);
        createUpdateAlarm(vehicle, tag.getTagId(), endTS, endTSms, false, 0);
        updateSnapShot(vehicle, tag, endTS, endTSms, 0);


        //ev_index,ON_time_sec, ON_time_ms ,OFF_time_sec, OFF_time_ms,active_ev_count,ev_code,status_info
        int eventIndex, numberOfEvents,  statusInfo;

        try {
            eventIndex = Integer.parseInt(dataValues[DiagnosticsConfig.DAIGD_EVENT_INDEX_POS]);
            numberOfEvents = Integer.parseInt(dataValues[DiagnosticsConfig.DAIGD_EVENT_NUMBER_OF_EVENTS_POS]);
            statusInfo = Integer.parseInt(dataValues[DiagnosticsConfig.DIAGD_EVENT_STATUS_POS]);
        }catch (NumberFormatException e){

            LOG.error("invalid format for end time stamp" + e);
            return false;
        }

        String vehicleID;

        try {
            vehicleID = vehicle.getVehicleId();
        } catch (NullPointerException e){
            LOG.error("vehicle got null" + e);
            return false;
        }

        //store the History Data
        AlarmValueHistoryInfo hist =  new AlarmValueHistoryInfo(tag.getTagId(), vehicleID, eventIndex,
                startTS, startTSms, endTS, endTSms,
                numberOfEvents, sourceTagId, statusInfo, tag);

        hist.create();
        Map<String, Object> myRestriction = new HashMap<>();
        myRestriction.put("eventIndex", eventIndex);
        myRestriction.put("tagId", tag.getTagId());
        myRestriction.put("vehicleId", vehicleID);
        myRestriction.put("startTimeStamp", startTS);
        myRestriction.put("startTSMilliseconds", startTSms);
        //readback
        hist = AlarmValueHistoryInfo.read(AlarmValueHistoryInfo.class, myRestriction);
        if(hist == null){
            LOG.error("AlarmTagHistoryInfo is null in DataBase!");
            return false;
        }

        if(hist.getMyTag() == null){
            LOG.error("tag is null in DataBase!");
            return false;
        }

        hist.setGuID(guId);
        //now send event to History Thread puller to store env data
        DataCollectorDiagnosticEventPuller.getInstance().putInEventBuffer(hist);


        return true;
    }

    private void createUpdateAlarm(Vehicle vehicle, Integer tagId, Integer timeStamp, Integer milliSeconds, boolean update, long value) {

        if(vehicle == null){
            return;
        }

        String vid = vehicle.getVehicleId();
        //if no exist create it
        Map<String, Object> myRestrictions = new HashMap<>();
        myRestrictions.put("tagId", tagId);
        myRestrictions.put("vehicleId", vid);
        myRestrictions.put("timeStamp", timeStamp);
        myRestrictions.put("milliSeconds", milliSeconds);

        HistoryAlarmTagValue historyAlarmTagValue = null;

        if(update){
            historyAlarmTagValue = HistoryAlarmTagValue.read(HistoryAlarmTagValue.class, myRestrictions);
            if(historyAlarmTagValue != null) {
                historyAlarmTagValue.setValue(value);
                historyAlarmTagValue.update();
                return;
            }
        }

        historyAlarmTagValue = new HistoryAlarmTagValue(vid, tagId, timeStamp, milliSeconds,value, 1);
        historyAlarmTagValue.create();
    }

    private void updateSnapShot(Vehicle vehicle, DataTag tag, Integer timeStamp, Integer TSMilliseconds, long value){

        Map<String, Object> myRestrictions = new HashMap<>();
        if(vehicle == null){
            LOG.debug("vehicle is null");
            return;
        }
        String vid = vehicle.getVehicleId();
        //check snapshot
        SnapShotAlarmTagValue snapShotAlarmTagValue = vehicle.getSnapShotValue(SnapShotAlarmTagValue.class,
                EventTypeEnum.TAG_DATA_TYPE_EVENT, tag.getTagId());

        if(snapShotAlarmTagValue == null){
            myRestrictions.clear();
            myRestrictions.put("tagId", tag.getTagId());
            myRestrictions.put("vehicleId", vid);
            //get it from database
            snapShotAlarmTagValue = SnapShotAlarmTagValue.read(SnapShotAlarmTagValue.class, myRestrictions);
            //again if not found in database create it
            if(snapShotAlarmTagValue == null){
                snapShotAlarmTagValue = new SnapShotAlarmTagValue(vid, tag.getTagId(), timeStamp, TSMilliseconds, value, 1, true);
                snapShotAlarmTagValue.create();
                LOG.debug("snapshot created " + tag.getTagId());
            }
            //then put it on the map
            vehicle.putSnapShotValue(EventTypeEnum.TAG_DATA_TYPE_EVENT, snapShotAlarmTagValue, true);
        } else {
            //otherwise exist in the map set new values
            snapShotAlarmTagValue.setTimeStamp(timeStamp);
            snapShotAlarmTagValue.setMilliSeconds(TSMilliseconds);
            snapShotAlarmTagValue.setValue(value);
            snapShotAlarmTagValue.setAlarmSyncStatus(AlarmSyncStatus.ALARM_SYNC_FRESH);

            snapShotAlarmTagValue.update();
            LOG.debug("snapshot updated " + tag.getTagId());
        }

        //trigger the update event
        List<Integer> updateItemList = new ArrayList<>();
        updateItemList.add(tag.getSourceTagId());

        WebManager.getInstance().sendDataUpdateEvent(vehicle, EventTypeEnum.TAG_DATA_TYPE_EVENT, timeStamp,  updateItemList);

    }

    private boolean parseResumeConnection(String guId, String[] frameValues) {
        //resume connection replace the hello message for suspended vehicles, so it must assign the connection to the vehicle
        DataCollectorFrameSender mySender = DataCollectorFrameSender.getInstance();
        String sequenceNumber = frameValues[DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS];
        String vID = frameValues[DiagnosticsConfig.MSG_RESUME_VEHICLE_ID_POS];

        //is some vehicle is already register then nack it
        if(DataCollectorSocket.getInstance().isVehicle(guId)){
            LOG.error("VID is already register");
            mySender.sendNack(guId, FunctionEnum.FC_RESUME_CONNECTION, ReturnCode.RET_HELLO_VID_ALREADY_REGISTER, sequenceNumber);
            return false;
        }

        Vehicle v =  WebManagerFacade.getInstance().getVehicle(vID);

        if(v == null){
            LOG.error(String.format("Vehicle not found %s", vID));
            mySender.sendNack(guId, FunctionEnum.FC_RESUME_CONNECTION, ReturnCode.RET_HELLO_VID_NOT_IN_CONFIGURATION, sequenceNumber);
            mySender.closeConnection(guId);
            return false;
        }

        Integer timeout;

        try{
            timeout = Integer.parseInt(frameValues[DiagnosticsConfig.MSG_RESUME_LS_TIMEOUT_POS]);
        }catch (NumberFormatException e){

            LOG.error(String.format("LS timeout not defined"));
            mySender.sendNack(guId, FunctionEnum.FC_RESUME_CONNECTION, ReturnCode.RET_HELLO_LS_TIMEOUT_NOT_SET, sequenceNumber);
            return false;
        }

        if(!DataCollectorSocket.getInstance().updateVehicle(guId, timeout, v)){
            LOG.error(String.format("not possible to store the vehicle %s", v.getVehicleId()));
            mySender.sendNack(guId, FunctionEnum.FC_RESUME_CONNECTION, ReturnCode.RET_HELLO_VEHICLE_NOT_SET, sequenceNumber);
            return false;
        }


        v.setStatus(VehicleStatusEnum.ST_ONLINE);

        //send ack
        mySender.sendAck(guId, FunctionEnum.FC_RESUME_CONNECTION, sequenceNumber);
        LOG.debug(String.format("Connection with vehicle %s was resumed", v.getVehicleId()));
        return true;

    }

    private void parseSuspendConnection(String guId, String[] frameValues) {

        DataCollectorFrameSender mySender = DataCollectorFrameSender.getInstance();
        if(frameValues.length < DiagnosticsConfig.SUSPEND_MESSAGE_SIZE){
            LOG.error("Invalid msg size " + FunctionEnum.FC_SUSPEND_CONNECTION.name());
            mySender.sendNack(guId, FunctionEnum.FC_SUSPEND_CONNECTION, ReturnCode.RET_INVALID_MESSAGE_SIZE, frameValues[DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS]);
            return;
        }

        //get vehicle
        Vehicle v = DataCollectorSocket.getInstance().findVehicleByGuId(guId);
        if(v == null){
            LOG.error("No vehicle found " + FunctionEnum.FC_SUSPEND_CONNECTION.name());
            mySender.sendNack(guId, FunctionEnum.FC_SUSPEND_CONNECTION, ReturnCode.RAT_SUSPEND_VEHICLE_NOT_FOUND, frameValues[DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS]);
            return;
        }

        int timeOut = 0;

        try {
            timeOut = Integer.parseInt(frameValues[DiagnosticsConfig.SUSPEND_MESSAGE_TIMEOUT_POS]);
        } catch (NumberFormatException e){
            this.LOG.error(String.format("Error parsing function %s with exception %s", FunctionEnum.FC_SUSPEND_CONNECTION.name(), e));
            mySender.sendNack(guId, FunctionEnum.FC_SUSPEND_CONNECTION, ReturnCode.RET_SUSPEND_INVALID_TIMEOUT, frameValues[DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS]);
            return;
        }

        if(timeOut <= 0 || timeOut > DiagnosticsConfig.SUSPEND_MAX_TIMEOUT){
            this.LOG.error(String.format("Timeout is invalid %d, should be > 0 and <= %d",
                    timeOut, DiagnosticsConfig.SUSPEND_MAX_TIMEOUT));
            mySender.sendNack(guId, FunctionEnum.FC_SUSPEND_CONNECTION, ReturnCode.RET_SUSPEND_INVALID_TIMEOUT, frameValues[DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS]);
            return;

        }

        //set the vehicle is suspend status
        v.setStatus(VehicleStatusEnum.ST_SUSPENDED);
        //start a timer for timeout with vehicle id
        DataCollectorTimer t = new DataCollectorTimer(v, timeOut);
        //when this timer finish and status still suspended set offline status
        v.setSuspendTimer(t);

        mySender.sendAck(guId, FunctionEnum.FC_SUSPEND_CONNECTION, frameValues[DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS]);
        this.LOG.debug(String.format("Vehicle %s connection is suspended for %d seconds", v.getVehicleId(), timeOut));
    }

    /**
     *
     * @param s -
     * @return
     */
    private String[] parseHeader(String s) {

        String[] frame = null;

        if(s == null){
			return frame;
		}
		
		 frame = s.split(DiagnosticsConfig.FrameSplitChar);
		
		if(frame.length < DiagnosticsConfig.MESSAGE_HEADER_SIZE){
			 
			return null;
		}
		
		return frame;
	}



	private void parseHello(String guId, String[] s) {
		 
		 
		DataCollectorFrameSender mySender = DataCollectorFrameSender.getInstance();
        String sequenceNumber = s[DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS];

		
		if(s.length < DiagnosticsConfig.HELLO_MESSAGE_SIZE){
            LOG.error("invalid hello msg size");
			mySender.sendNack(guId, FunctionEnum.FC_HELLO, ReturnCode.RET_INVALID_MESSAGE_SIZE, sequenceNumber);
			return;
		}

        //life sign time out
        Integer closeTimeOut = 0;
        try {
            closeTimeOut = Integer.parseInt(s[DiagnosticsConfig.MSG_HELLO_VEHICLE_LS_TIMEOUT_POS]);
        } catch (NumberFormatException e){
            LOG.error("invalid closeTimeOut");
            closeTimeOut = DiagnosticsConfig.DEFAULT_CLOSE_TIMEOUT;
        }

        //is some vehicle is already register then nack it
        if(DataCollectorSocket.getInstance().isVehicle(guId)){
            LOG.error("VID is already register");
            mySender.sendNack(guId, FunctionEnum.FC_HELLO, ReturnCode.RET_HELLO_VID_ALREADY_REGISTER, sequenceNumber);
            return;
        }


		String vID = s[DiagnosticsConfig.MSG_HELLO_VEHICLE_ID_POS];
        Vehicle v = WebManagerFacade.getInstance().getVehicle(vID);

		if(v == null){
            LOG.error(String.format("VID not found %s", vID));
			mySender.sendNack(guId, FunctionEnum.FC_HELLO, ReturnCode.RET_HELLO_VID_NOT_IN_CONFIGURATION, sequenceNumber);
			mySender.closeConnection(guId);
			return;
		}

        if(!DataCollectorSocket.getInstance().updateVehicle(guId, closeTimeOut, v)){
            LOG.error(String.format("not possible to store the vehicle %s", v.getVehicleId()));
            mySender.sendNack(guId, FunctionEnum.FC_HELLO, ReturnCode.RET_HELLO_VEHICLE_NOT_SET, sequenceNumber);
            return;
        }
		
		//if ok store vehicleID
		//sms number
		v.setSmsNumber(s[DiagnosticsConfig.MSG_HELLO_SMS_NUMBER_POS]);
        v.setStatus(VehicleStatusEnum.ST_ONLINE);
        v.update();
        //check diagd configuration

        try {

            String diagdInit = s[DiagnosticsConfig.MSG_HELLO_DIAG_INIT_POS];
            if(diagdInit.compareToIgnoreCase("YES") == 0){
                WebManager.getInstance().backupOldEventData(v);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException ex){

            LOG.error(ex);

        }
        //send ack
		mySender.sendAck(guId, FunctionEnum.FC_HELLO, sequenceNumber);
        LOG.debug(String.format("Hello message ack for vehicle %s", v.getVehicleId()));

	}

    private void parseLifeSign(String guId, String[] frameValues) {
		
		DataCollectorFrameSender mySender = DataCollectorFrameSender.getInstance();
        String sequenceNumber = frameValues[DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS];
		
		if(frameValues.length < DiagnosticsConfig.LIFE_SIGN_MESSAGE_SIZE){
			//mySender.sendNack(guId, FunctionEnum.FC_LIFE_SIGN, ReturnCode.RET_INVALID_MESSAGE_SIZE, frameValues[DataCollectorConfig.MSG_HEADER_SN_POS]);
			LOG.error("Invalid Life Sign Size");
			return;
		}

        DataCollectorSocket.getInstance().refreshCountDown(guId);
        storeSystemDataFromLifeSign(guId, frameValues);
        storeOfflineProcessData(guId, frameValues);
        mySender.sendAck(guId,FunctionEnum.FC_LIFE_SIGN, sequenceNumber);
		
	}

    private void storeOfflineProcessData(String guId, String[] frameValues) {

        Vehicle vehicle = DataCollectorSocket.getInstance().findVehicleByGuId(guId);

        if(vehicle == null){
            return;
        }

        if( DiagnosticsConfig.MSG_LIFE_SIGN_PD_BUFFER_POS >= frameValues.length){
            LOG.error("The LS frame doesn't have Offline process data value");
            return;
        }

        String processDataValues;

        try {
            processDataValues = frameValues[DiagnosticsConfig.MSG_LIFE_SIGN_PD_BUFFER_POS];
        } catch (ArrayIndexOutOfBoundsException ex){
            LOG.error(ex);
            return;
        }

        Integer timeStamp = Integer.parseInt(frameValues[DiagnosticsConfig.MSG_HEADER_TIME_STAMP_POS]);

        //processDataValues are spliced by ,
        String [] pdValues = processDataValues.split(DiagnosticsConfig.PD_OFFLINE_BUFFER_SPLITTER);

        for(String pd : pdValues){
            parseProcessDataElement(vehicle, timeStamp, pd);
        }

    }

    private void storeSystemDataFromLifeSign(String guId, String[] frameValues) {

        int timeStamp;
        try{
            timeStamp = Integer.parseInt(frameValues[DiagnosticsConfig.MSG_HEADER_TIME_STAMP_POS]);
        } catch (NumberFormatException e){
            LOG.error(e);
            return;
        }

        Vehicle vehicle = DataCollectorSocket.getInstance().findVehicleByGuId(guId);

        if(vehicle == null){
            LOG.error("vehicle is null on store LS values");
            return;
        }

        List<Integer> listOfUpdateItems = new ArrayList<Integer>();

        int frameSize = frameValues.length;


        for(DCLifeSignParamEnum p : DCLifeSignParamEnum.values()){



            if(p.equals(DCLifeSignParamEnum.TRAIN_STATUS)){
                continue;
            }

            long value;
            Integer index;
            index = p.ordinal();

            if(index + DiagnosticsConfig.MESSAGE_HEADER_SIZE >= frameSize){
                continue;
            }

            try {

                String strValue = frameValues[index + DiagnosticsConfig.MESSAGE_HEADER_SIZE];
                if("".equals(strValue)){
                    continue;
                }
                value = Long.parseLong(strValue);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
                LOG.error(e);
                continue;
            }
            DataTag t = DCLifeSign.getInstance().get(index);
            if(t == null){
                continue;
            }
            vehicle.putSnapShotValue(EventTypeEnum.TAG_DATA_TYPE_SYSTEM, t, timeStamp, DiagnosticsConfig.DC_DEFAULT_MS_VALUE, value);
            listOfUpdateItems.add(t.getTagId());
        }

        //LOG.debug("send LS update event");
        WebManager.getInstance().sendDataUpdateEvent(vehicle, EventTypeEnum.TAG_DATA_TYPE_SYSTEM, timeStamp, listOfUpdateItems);
    }

    private boolean parseProcessData(String guId, String[] frameValues) {

        //check if vehicle is in configuration
        Vehicle vehicle = DataCollectorSocket.getInstance().findVehicleByGuId(guId);

        if(vehicle == null){
            DataCollectorFrameSender.getInstance().sendNack(guId, FunctionEnum.FC_PROCESS_DATA,
                    ReturnCode.RET_VEHICLE_NOT_INITIALIZED, frameValues[DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS]);
            return false;
        }

        Integer timeStamp = Integer.parseInt(frameValues[DiagnosticsConfig.MSG_HEADER_TIME_STAMP_POS]);
        List<Integer> listOfUpdateItems = new ArrayList<Integer>();

        String [] pdValues;

        try {
            pdValues  = frameValues[DiagnosticsConfig.MESSAGE_HEADER_SIZE].split(DiagnosticsConfig.PD_OFFLINE_BUFFER_SPLITTER);
        } catch (ArrayIndexOutOfBoundsException ex){
            LOG.error(ex);
            return false;
        }


        int numberOfPDs = pdValues.length;

        for(int i = 0; i < numberOfPDs; i++){
            String parsePD =  pdValues[i];
            Integer result = parseProcessDataElement(vehicle, timeStamp, parsePD);
            if(result!= null){
                listOfUpdateItems.add(result);
            }
        }
        StringBuilder msg = new StringBuilder();
        for(Integer myInt : listOfUpdateItems){
            msg.append(String.format("%d,", myInt));
        }
        LOG.debug(String.format("send %d elements : %s",listOfUpdateItems.size(), msg));
        WebManager.getInstance().sendDataUpdateEvent(vehicle, EventTypeEnum.TAG_DATA_TYPE_PD, timeStamp, listOfUpdateItems);

        return true;
    }

    private Integer parseProcessDataElement(Vehicle vehicle, int timeStamp, String parsePD) {
        //split
        String[] pdValues = parsePD.split(DiagnosticsConfig.PROCESS_DATA_SPLITTER);

        if(pdValues.length < DiagnosticsConfig.PROCESS_DATA_VALUE_PARAM_SIZE){
            LOG.error("invalid process data param size");
            return null;
        }
        //check id
        int id;
        int value;

        try{
             id = Integer.parseInt(pdValues[DiagnosticsConfig.PROCESS_DATA_VALUE_PARAM_ID_POS]);
             value = Integer.parseInt(pdValues[DiagnosticsConfig.PROCESS_DATA_VALUE_PARAM_VALUE_POS]);

        } catch (NumberFormatException e){
            LOG.error("parseProcessDataElement:Invalid format number for id or value");
            return null;
        }

        if(id < 0 || timeStamp< 0){
            LOG.error("invalid id or timestamp");
            return null;
        }

        //find matching tag id
        DataTag t = WebManager.getInstance().findTagBySourceTagId(EventTypeEnum.TAG_DATA_TYPE_PD, vehicle.getConfigurationId(), id);

        if(t == null){
            LOG.error(String.format("Tag type %d and sourceId %d ConfigurationID %d not found",
                    EventTypeEnum.TAG_DATA_TYPE_PD.getValue(), id, vehicle.getConfigurationId()));

            return null;
        }

        vehicle.putSnapShotValue(EventTypeEnum.TAG_DATA_TYPE_PD, t, timeStamp, DiagnosticsConfig.DC_DEFAULT_MS_VALUE, value);
        return t.getSourceTagId();
    }

    private boolean parseEventData(String guId, String[] frameValues) {

        //get the number of PD elements and parseStringToBigDecimal it
        Integer numberOfPDs;
        try{
            numberOfPDs = Integer.parseInt(frameValues[DiagnosticsConfig.MSG_HEADER_N_PARAMS_POS]);
        } catch (NumberFormatException e){
            LOG.error("parseEventData:Invalid format number for id or value");
            return false;
        }

        if(numberOfPDs<=0){
            LOG.error("parseEventData number of process data parameters is invalid");
            return false;
        }

        if(frameValues.length < (numberOfPDs + DiagnosticsConfig.MESSAGE_HEADER_SIZE)){
            LOG.error("the number of event data parameters is more than the actual frame data");
            return false;
        }
        Integer timeStamp = Integer.parseInt(frameValues[DiagnosticsConfig.MSG_HEADER_TIME_STAMP_POS]);

        //check if vehicle is in configuration
        Vehicle vehicle =DataCollectorSocket.getInstance().findVehicleByGuId(guId);
        if(vehicle == null){
            DataCollectorFrameSender.getInstance().sendNack(guId, FunctionEnum.FC_EVENT_DATA,
                    ReturnCode.RET_VEHICLE_NOT_INITIALIZED, frameValues[DiagnosticsConfig.MSG_HEADER_SEQUENCE_NUMBER_POS]);
            return false;
        }

        List<Integer> listOfUpdateItems = new ArrayList<Integer>();

        for(int i = 0; i < numberOfPDs; i++){
            String parseEVT =  frameValues[DiagnosticsConfig.MESSAGE_HEADER_SIZE + i];

            int id =parseEventDataElement(vehicle, parseEVT);

            if(id>=0){
                listOfUpdateItems.add(id);
            }

        }

        WebManager.getInstance().sendDataUpdateEvent(vehicle, EventTypeEnum.TAG_DATA_TYPE_EVENT, timeStamp, listOfUpdateItems);
        return true;
    }


    private int parseEventDataElement(Vehicle vehicle, String parseEVT) {

        //split
        String[] pdValues = parseEVT.split(DiagnosticsConfig.PROCESS_DATA_SPLITTER);
        if(pdValues.length < DiagnosticsConfig.EVENT_DATA_VALUE_PARAM_SIZE){
            LOG.error("invalid event data param size");
            return -1;
        }

        int sourceTagId;
        long value;
        int timeStamp;

        try {
            //check id
            sourceTagId = Integer.parseInt(pdValues[DiagnosticsConfig.EVENT_DATA_VALUE_PARAM_ID_POS]);
            value = Long.parseLong(pdValues[DiagnosticsConfig.EVENT_DATA_VALUE_PARAM_VALUE_POS]);
            timeStamp = Integer.parseInt(pdValues[DiagnosticsConfig.EVENT_DATA_VALUE_PARAM_TIMESTAMP_POS]);
        } catch (NumberFormatException e){
            LOG.error("parseEventDataElement:Invalid format number for id or value");
            return -1;
        }

        if(sourceTagId < 0 || value <0 || timeStamp< 0){
            LOG.error("invalid id, value or timestamp");
            return -1;
        }

        //find matching tag id
        DataTag t = WebManager.getInstance().findTagBySourceTagId(EventTypeEnum.TAG_DATA_TYPE_EVENT,vehicle.getConfigurationId(), sourceTagId);

        if(t == null){
            LOG.error(String.format("no matching Tag for the data with id=%d and configuration=%d", sourceTagId, vehicle.getConfigurationId()));
            return -1;
        }

        //create update value list
        //DataCollectorManager.getInstance().addTagValue(id, value, timeStamp);

        vehicle.putSnapShotValue(EventTypeEnum.TAG_DATA_TYPE_EVENT, t, timeStamp, DiagnosticsConfig.DC_DEFAULT_MS_VALUE, value);


        //create update value list
        //DataCollectorManager.getInstance().addTagValue(id, value, ts);

        return t.getTagId();
    }

    public boolean addListener(DCParserAckNackEventHandler handler){
        return this.listeners.add(handler);
    }

    public boolean removeListener(DCParserAckNackEventHandler handler){
        return this.listeners.remove(handler);
    }


    private void handleAckNackForStartVncServer(FunctionEnum sourceFunction, FunctionEnum result, String guId) {

        //find vehicle
        Vehicle v = DataCollectorSocket.getInstance().findVehicleByGuId(guId);
        if(v == null){
            LOG.debug("handleAckNackForStartVncServer vehicle not found");
            return;
        }

        fireEvent(new DCParserAckNackEvent(v.getVehicleId(), sourceFunction, result));

    }

    void fireEvent(final DCParserAckNackEvent event){

        LOG.debug(String.format("we have %d listeners", listeners.size()));
        for(final DCParserAckNackEventHandler handler : listeners){

            Runnable runnable = new Runnable() {
                public void run() {

                    handler.handleEvent(event);

                }

            };
            new Thread(runnable).start();
        }
    }


}
