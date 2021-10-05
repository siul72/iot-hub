package co.luism.lukisoftiot.datacollector.common;

import co.luism.lukisoftiot.datacollector.DataCollectorSocket;
import co.luism.lukisoftiot.common.DiagDFunctionEnum;
import co.luism.lukisoftiot.common.FunctionEnum;
import co.luism.lukisoftiot.common.ReturnCode;
import co.luism.lukisoftiot.datacollector.AlarmEnvironmentDataRequester;

import java.util.List;

public final class DataCollectorFrameSender {
	private static DataCollectorFrameSender instance = null;
	 
	public static DataCollectorFrameSender getInstance() {
		if(instance == null){
			instance = new DataCollectorFrameSender();
		}
		return instance;
	}

	public void sendNack(String guid, FunctionEnum msgID, ReturnCode ret, String s) {
		
				
		DataCollectorSocket.getInstance().sendFrame(guid, MessageFactory.getNackMessage(msgID, ret, s));
		 
	}

	public void sendAck(String guid, FunctionEnum fcHello, String sCount) {
		 
		DataCollectorSocket.getInstance().sendFrame(guid, MessageFactory.getAckMessage(fcHello, sCount));
		
	}

	public void closeConnection(String guId) {
		DataCollectorSocket.getInstance().closeClient(guId);
		
	}


    public boolean sendStartProcessData(String guid, String updateInterval) {
        return DataCollectorSocket.getInstance().sendFrame(guid, MessageFactory.getSendProcessDataMessage(updateInterval));
    }

    public boolean sendStopProcessData(String connectionGUID) {

        return DataCollectorSocket.getInstance().sendFrame(connectionGUID, MessageFactory.getSendStopProcessDataMessage());
    }

    public void sendGetNextAlarm(DataCollectorClient c) {

        DataCollectorSocket.getInstance().sendFrame(c, MessageFactory.getSendGetNextAlarm());
    }

    public void sendAckToDiagnosticsDeamon(String guId, DiagDFunctionEnum fcGetNextEventResponse, String sequenceNumber) {
        DataCollectorSocket.getInstance().sendFrame(guId, MessageFactory.getSendAckToDiagDeamon());
    }

    public boolean sendGetEnvData(AlarmEnvironmentDataRequester element) {
       return DataCollectorSocket.getInstance().sendFrame(element.getGuID(),
                MessageFactory.getSendEnvDataToDiagDeamon(element.getEnvDataStatus(),
                        element.getEventIndex(), element.getCategoryIndex()));
    }

    public boolean sendVncStopMessage(String guId, Integer port) {

        return DataCollectorSocket.getInstance().sendFrame(guId, MessageFactory.getSendVncStopMessage(port));
    }

    public boolean sendStartVNCMessage(String guId, Integer port) {
        return DataCollectorSocket.getInstance().sendFrame(guId, MessageFactory.getSendVncStartMessage(port));
    }

    public boolean sendConfigureProcessData(String connectionGUID, List<Integer> myOfflineList, List<Integer> myOnlineList) {
        return DataCollectorSocket.getInstance().sendFrame(connectionGUID, MessageFactory.getSendConfigureProcessDataMessage(myOfflineList, myOnlineList));
    }
}
