package co.luism.lukisoftiot.datacollector.common;

import co.luism.lukisoftiot.common.DiagnosticsConfig;
import co.luism.lukisoftiot.enterprise.Vehicle;
import org.apache.log4j.Logger;

import java.net.Socket;
import java.util.UUID;


public final class DataCollectorClient {

    private static final Logger LOG = Logger.getLogger(DataCollectorClient.class);
    private final Socket clientSocket;
	private final UUID index = java.util.UUID.randomUUID();
	private Vehicle myVehicle = null;
	private Integer closeTimeOut = 0;
    private Integer currentTimeOut = DiagnosticsConfig.EXTRA_TIMEOUT_VALUE;
    private Integer configuredAlarmPullCount = DiagnosticsConfig.DEFAULT_ALARM_PULL_COUNT;
    private Integer currentAlarmCount = DiagnosticsConfig.DEFAULT_ALARM_PULL_COUNT;
	
	public DataCollectorClient(final Socket so){
		 
		this.clientSocket = so;

		
	}
	
	public UUID getIndex() {
		return index;
	}
	 
	public Socket getSocket() {
		return clientSocket;
	}

	public Vehicle getMyVehicle() {
		return myVehicle;
	}

	public void setMyVehicle(Vehicle myVehicle) {
		this.myVehicle = myVehicle;
	}

    public Integer getCloseTimeOut() {
        LOG.debug("get config time out " + this.closeTimeOut);
        return closeTimeOut;
    }

    public void setCloseTimeOut(Integer closeTimeOut) {
        this.closeTimeOut = closeTimeOut;
        LOG.debug("set config time out to:" + this.closeTimeOut);
        resetCurrentTimeOut();
    }

    public Integer getCurrentTimeOut() {
        return currentTimeOut;
    }

    public void decrementCurrentTimeOut()
    {
        if(this.currentTimeOut > 0){
            this.currentTimeOut--;
        }

    }

    public void resetCurrentTimeOut() {

        updateTimeOut();
    }

    public void updateTimeOut() {
        this.currentTimeOut = closeTimeOut + DiagnosticsConfig.EXTRA_TIMEOUT_VALUE;
        LOG.debug("reset timeout to " + this.currentTimeOut);
    }

    public Integer getConfiguredAlarmPullCount() {
        return configuredAlarmPullCount;
    }

    public void setConfiguredAlarmPullCount(Integer configuredAlarmPullCount) {
        this.configuredAlarmPullCount = configuredAlarmPullCount;
    }

    public Integer getCurrentAlarmCount() {
        return currentAlarmCount;
    }

    public void setCurrentAlarmCount(Integer currentAlarmCount) {
        this.currentAlarmCount = currentAlarmCount;
    }
}
