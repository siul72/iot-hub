package co.luism.datacollector;

import co.luism.datacollector.common.DataCollectorFrameSender;
import co.luism.diagnostics.common.DiagnosticsConfig;
import co.luism.diagnostics.common.EventEnvDataStatus;
import co.luism.diagnostics.common.GetEnvStatusEvent;
import co.luism.diagnostics.enterprise.AlarmCategory;
import co.luism.diagnostics.enterprise.AlarmValueHistoryInfo;
import co.luism.diagnostics.interfaces.IGetEnvStatusEventHandler;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by luis on 04.11.14.
 */
public class AlarmEnvironmentDataRequester extends AlarmValueHistoryInfo {
    private static final Logger LOG = Logger.getLogger(AlarmEnvironmentDataRequester.class);

    private final AlarmCategory myCategory;
    private final Set<IGetEnvStatusEventHandler> getEnvStatusEventHandlers = new HashSet<>();
    private EventEnvDataStatus envDataStatus = EventEnvDataStatus.GET_NONE;
    private Integer requestCount = 0;
    private Integer packetCount = 0;
    private Timer timer;

    public AlarmEnvironmentDataRequester(AlarmValueHistoryInfo take, AlarmCategory myCategory) {
        super(take);
        this.myCategory = myCategory;
        this.requestCount =  DiagnosticsConfig.ENV_DATA_REQUEST_COUNTER;
        take.getGuID();
        setEnvDataStatus(EventEnvDataStatus.GET_START);
    }

    public void start(){
        //setEnvDataStatus(EventEnvDataStatus.GET_START);
        startTimerSendGetEnvData();
    }

    public void addEnvStatusListener(IGetEnvStatusEventHandler getEnvStatusEventHandler){
        LOG.debug("add listener");
        this.getEnvStatusEventHandlers.add(getEnvStatusEventHandler);
    }
    public void removeEnvStatusListener(IGetEnvStatusEventHandler getEnvStatusEventHandler){
        LOG.debug("remove listener");
        this.getEnvStatusEventHandlers.remove(getEnvStatusEventHandler);
    }

    public Integer getCategoryIndex() {
        return this.myCategory.getCategoryIndex();
    }
    public EventEnvDataStatus getEnvDataStatus() {
        return envDataStatus;
    }
    public void setEnvDataStatus(EventEnvDataStatus envDataStatus) {
        this.envDataStatus = envDataStatus;
        //fire event
        fireEvent(new GetEnvStatusEvent(this.envDataStatus, this.getId()));

    }

    private void startTimerSendGetEnvData() {
        int milliSeconds = 0;
        //if not post data schedule a default time
        if(this.getMyTag().isPostData() && this.envDataStatus == EventEnvDataStatus.GET_START) {
            milliSeconds = myCategory.getMyBuffer().getSampleMilliSeconds() + myCategory.getMyBuffer().getSampleSeconds() * 1000;
            milliSeconds = milliSeconds * myCategory.getMyBuffer().getNumberOfSamples();
        }

        milliSeconds = milliSeconds + DiagnosticsConfig.DC_DEFAULT_EXTRA_TIME_GET_ENV_DATA;

        if(timer == null){

            timer = new Timer();
            //LOG.debug("timer was created");
            timer.schedule(new TimeOutTask(), milliSeconds);
            LOG.debug(String.format("start GETENVDATA TIMER with %d ms " ,milliSeconds));
        } else {
            LOG.warn("timer is running");
        }


    }

    public Integer getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Integer requestCount) {
        this.requestCount = requestCount;
    }

    public void cancelTimer(){
        LOG.info(String.format("Timer canceled index %d", getEventIndex()));
        timer.cancel();
        timer.purge();
    }

    private void fireEvent(final GetEnvStatusEvent eventPullerStatusEvent) {
            for(final IGetEnvStatusEventHandler envHandler : this.getEnvStatusEventHandlers){
                Runnable runnable = new Runnable() {
                    public void run() {

                        envHandler.handleEnvStatus(eventPullerStatusEvent);

                    }

                };
                new Thread(runnable).start();
            }

    }

    public void incrementPacketCount() {
        this.packetCount++;

    }

    public Integer getPacketCount() {
        return packetCount;
    }

    class TimeOutTask extends TimerTask {

        public void run() {
            LOG.info(String.format("Time to get env data for %d:%d", getEventIndex(), getCategoryIndex()));
            if(!DataCollectorFrameSender.getInstance().sendGetEnvData(AlarmEnvironmentDataRequester.this)){
                //the channel was closed
                requestCount = 0;
            }

            timer.cancel();
            timer.purge();
            timer = null;

            //LOG.debug("timer was deleted");

        }
    }



}
