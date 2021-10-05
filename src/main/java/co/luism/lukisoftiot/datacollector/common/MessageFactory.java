package co.luism.lukisoftiot.datacollector.common;

import co.luism.lukisoftiot.common.*;

import java.util.List;

public final class MessageFactory {

    MessageFactory(){

    }
	
	private static int sequenceNumber = 0;
    private static Object sendGetNextAlarm;

    public static String getNackMessage(FunctionEnum messageID, ReturnCode causeID, String sequenceNumber){
		
		
		StringBuilder sendFrame = new StringBuilder();
		sendFrame.append(createHeader(FunctionEnum.FC_NACK, DiagnosticsConfig.NACK_PARAM_SIZE));
		sendFrame.append(messageID.getValue());
		sendFrame.append(DiagnosticsConfig.FrameSplitChar);
		sendFrame.append(causeID.getValue());
		sendFrame.append(DiagnosticsConfig.FrameSplitChar);
		sendFrame.append(sequenceNumber);
		sendFrame.append(DiagnosticsConfig.FrameSplitChar);
		
		return sendFrame.toString();
	}
	
	
	private static String createHeader(FunctionEnum f, int fSize){
		
		long unixTime = System.currentTimeMillis() / 1000L;
		 
		return String.format("%s%s%d%s%d%s%d%s",
				f.getValue(), DiagnosticsConfig.FrameSplitChar,
				fSize, DiagnosticsConfig.FrameSplitChar,
				unixTime, DiagnosticsConfig.FrameSplitChar,
				sequenceNumber++, DiagnosticsConfig.FrameSplitChar
				);
		 
		
	}


	public static String getAckMessage(FunctionEnum fcHello, String sCount) {
		
		StringBuilder sendFrame = new StringBuilder();
		sendFrame.append(createHeader(FunctionEnum.FC_ACK, DiagnosticsConfig.ACK_PARAM_SIZE));
		sendFrame.append(fcHello.getValue());
		sendFrame.append(DiagnosticsConfig.FrameSplitChar);
		sendFrame.append(sCount);
		sendFrame.append(DiagnosticsConfig.FrameSplitChar);
		
		return sendFrame.toString();
	}

    public static String getSendProcessDataMessage(String updateInterval) {

        StringBuilder sendFrame = new StringBuilder();
        sendFrame.append(createHeader(FunctionEnum.FC_START_PROCESS_DATA, DiagnosticsConfig.START_PROCESS_DATA_PARAM_SIZE));
        //param 1
        sendFrame.append(updateInterval);
        sendFrame.append(DiagnosticsConfig.FrameSplitChar);

        return sendFrame.toString();
    }

    public static String getSendStopProcessDataMessage() {

        return createHeader(FunctionEnum.FC_STOP_PROCESS_DATA, DiagnosticsConfig.STOP_PROCESS_DATA_PARAM_SIZE) + 0 + DiagnosticsConfig.FrameSplitChar;
    }

    public static String getSendGetNextAlarm() {

        StringBuilder sendFrame = new StringBuilder();
        sendFrame.append(createHeader(FunctionEnum.FC_DIAGD_REQUEST, DiagnosticsConfig.DIAGD_REQUEST_DATA_PARAM_SIZE));
        //param1 <getNewEvents>
        sendFrame.append(DiagDFunctionEnum.FC_GET_NEXT_EVENT.getValue());
        sendFrame.append(DiagnosticsConfig.FrameSplitChar);
        return sendFrame.toString();
    }

    public static String getSendAckToDiagDeamon() {

        StringBuilder sendFrame = new StringBuilder();
        sendFrame.append(createHeader(FunctionEnum.FC_DIAGD_REQUEST, DiagnosticsConfig.DIAGD_REQUEST_DATA_PARAM_SIZE));
        //param1 <ack>
        sendFrame.append(DiagDFunctionEnum.FC_ACK.getValue());
        sendFrame.append(DiagnosticsConfig.FrameSplitChar);
        return sendFrame.toString();

    }

    public static String getSendEnvDataToDiagDeamon(EventEnvDataStatus envDataStatus, Integer eventIndex, Integer categoryIndex) {
        //GetEnvData;event_index;event_category_number>

        return createHeader(FunctionEnum.FC_DIAGD_REQUEST, DiagnosticsConfig.DIAGD_REQUEST_DATA_PARAM_SIZE) + DiagDFunctionEnum.FC_GET_ENV_DATA.getValue() + String.format(";%d;%d;%d>", eventIndex, categoryIndex, envDataStatus.ordinal());

    }

    public static String getSendVncStopMessage(Integer port) {

        //param 1 - port
        return createHeader(FunctionEnum.FC_STOP_VNC_SERVER, DiagnosticsConfig.STOP_VNC_SERVER_SIZE) + port + DiagnosticsConfig.FrameSplitChar;
    }

    public static String getSendVncStartMessage(Integer port) {
        //param 1 - port
        return createHeader(FunctionEnum.FC_START_VNC_SERVER, DiagnosticsConfig.START_VNC_SERVER_SIZE) + port + DiagnosticsConfig.FrameSplitChar;
    }

    public static String getSendConfigureProcessDataMessage(List<Integer> myOfflineList, List<Integer> myOnlineList) {
        StringBuilder sendFrame = new StringBuilder();
        sendFrame.append(createHeader(FunctionEnum.FC_CONFIGURE_PROCESS_DATA, DiagnosticsConfig.CONFIGURE_PROCESS_DATA_SIZE));
        //param 1 - Offline Process Data Ids
        for(Integer i : myOfflineList){
            sendFrame.append(i).append(DiagnosticsConfig.PD_OFFLINE_BUFFER_SPLITTER);
        }
        sendFrame.append(DiagnosticsConfig.FrameSplitChar);
        for(Integer i : myOnlineList){
            sendFrame.append(i).append(DiagnosticsConfig.PD_OFFLINE_BUFFER_SPLITTER);
        }
        sendFrame.append(DiagnosticsConfig.FrameSplitChar);

        return sendFrame.toString();
    }
}
