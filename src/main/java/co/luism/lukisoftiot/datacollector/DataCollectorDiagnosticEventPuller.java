package co.luism.lukisoftiot.datacollector;
/*
  ____        _ _ _                   _____           _
 |  __ \     (_) | |                 / ____|         | |
 | |__) |__ _ _| | |_ ___  ___      | (___  _   _ ___| |_ ___ _ __ ___  ___
 |  _  // _` | | | __/ _ \/ __|      \___ \| | | / __| __/ _ \ '_ ` _ \/ __|
 | | \ \ (_| | | | ||  __/ (__       ____) | |_| \__ \ ||  __/ | | | | \__ \
 |_|  \_\__,_|_|_|\__\___|\___|     |_____/ \__, |___/\__\___|_| |_| |_|___/
                                            __/ /
 Railtec Systems GmbH                      |___/
 6052 Hergiswil

 SVN file informations:
 Subversion Revision $Rev: $
 Date $Date: $
 Commmited by $Author: $
*/

import co.luism.lukisoftiot.common.WatchDogClient;
import co.luism.lukisoftiot.datacollector.common.DCDPullerEvent;
import co.luism.lukisoftiot.datacollector.common.DCDPullerEventHandler;
import co.luism.lukisoftiot.datacollector.common.DataCollectorClient;
import co.luism.lukisoftiot.datacollector.common.DataCollectorFrameSender;
import co.luism.lukisoftiot.common.DiagnosticsConfig;
import co.luism.lukisoftiot.common.EventEnvDataStatus;
import co.luism.lukisoftiot.enterprise.AlarmCategory;
import co.luism.lukisoftiot.enterprise.AlarmValueHistoryInfo;
import co.luism.lukisoftiot.webapputils.WebAppUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * webmanager
 * co.luism.lukisoftiot.datacollector
 * Created by luis on 14.10.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */
public class DataCollectorDiagnosticEventPuller {
    public static final DataCollectorDiagnosticEventPuller instance = new DataCollectorDiagnosticEventPuller();
    private boolean runLoop = true;
    private static final Logger LOG = Logger.getLogger(DataCollectorDiagnosticEventPuller.class);
    private static Thread myThreadGetEvents;
    private static Thread myThreadGetEnvData;
    private final LinkedBlockingQueue<Object> eventBuffer = new LinkedBlockingQueue<>(500);
    private final Map<Integer, List<AlarmEnvironmentDataRequester>> alarmEnvironmentDataRequesterMap = new HashMap<>();
    private final Map<String, Set<DCDPullerEventHandler>> dcdEventPullerHandlerListeners = new HashMap<>();

    DataCollectorDiagnosticEventPuller(){

    }

    public void init(){
        runLoop = true;
        myThreadGetEvents = new Thread(new GetEventsRunnable());
        myThreadGetEvents.setName(GetEventsRunnable.class.getSimpleName());
        myThreadGetEvents.start();

        myThreadGetEnvData = new Thread (new GetEnvDataRunnable());
        myThreadGetEnvData.setName(GetEnvDataRunnable.class.getSimpleName());
        myThreadGetEnvData.start();

    }

    public static DataCollectorDiagnosticEventPuller getInstance(){
        return instance;
    }

    public void close(){
        runLoop = false;
        putInEventBuffer(new AlarmValueHistoryInfo());
        while (myThreadGetEvents.isAlive()) {
            LOG.debug("Still waiting...");
            // Wait maximum of 1 second
            // for MessageLoop thread
            // to finish.
            try {
                myThreadGetEvents.join(1000);
                myThreadGetEnvData.join(1000);
            } catch (InterruptedException e) {
                LOG.error(e);
            }

            if ( myThreadGetEvents.isAlive()) {
                LOG.debug("Tired of waiting!");
                myThreadGetEvents.interrupt();
                myThreadGetEnvData.interrupt();
                // Shouldn't be long now
                // -- wait indefinitely
                try {
                    myThreadGetEvents.join();
                    myThreadGetEnvData.join();
                } catch (InterruptedException e) {
                    LOG.error(e);
                }
            }
        }

        LOG.info("<<close()");
    }

    public List<AlarmEnvironmentDataRequester> getRequesterList(Integer alarmInfoId) {

        return alarmEnvironmentDataRequesterMap.get(alarmInfoId);

    }

    public void stopPulling(String connectionId) {

        List<AlarmEnvironmentDataRequester> myAlarmList =  findAlarmRequester(connectionId);

        for(AlarmEnvironmentDataRequester requester : myAlarmList){
            removeSendEnvironmentData(requester);
        }

    }

    class GetEventsRunnable implements Runnable {
        private WatchDogClient myWatchDog = new WatchDogClient(GetEventsRunnable.class.getSimpleName());
        @Override
        public void run() {
            //in a loop
            LOG.info(GetEventsRunnable.class.getSimpleName() + " Thread started");
            myWatchDog.init();
            myWatchDog.register(10);
            while (runLoop) {

                //check all connections
                //if connection as vehicle
                Set<DataCollectorClient> myConnections = DataCollectorSocket.getInstance().getConnectedVehicleSet();

                //ask for all alarms until is synchronized
                for (DataCollectorClient c : myConnections) {

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        LOG.debug("bread sleep was interrupted " + e);
                        break;
                    }

                    if(!runLoop){
                        break;
                    }

                    Integer count = c.getCurrentAlarmCount();
                    if(count > 0){
                        c.setCurrentAlarmCount(--count);
                        continue;
                    }

                    c.setCurrentAlarmCount(c.getConfiguredAlarmPullCount());
                    //   DataCollectorFrameSender.getInstance().sendGetAllAlarms(c);
                    DataCollectorFrameSender.getInstance().sendGetNextAlarm(c);

                    if(!runLoop){
                        break;
                    }
                }

                //ask for new alarms after is sync
                //store alarm in database
                //send events for new alarms
                //when finish to sync send event show that is sync
                //if sync read table
                //if not sync wait for sync event to read table

                if(!myWatchDog.update()){
                    myWatchDog.init();
                    myWatchDog.register(10);
                }

                //sleep for a while
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOG.debug("Sleep was interrupted " + e);
                }



            }

            LOG.info("GetEventsRunnable Thread Exit");
        }

    }

    private class GetEnvDataRunnable implements Runnable {

        private WatchDogClient myWatchDog = new WatchDogClient(GetEnvDataRunnable.class.getSimpleName());

        @Override
        public void run() {
            LOG.info(GetEnvDataRunnable.class.getSimpleName() + " Thread started");
            myWatchDog.init();
            myWatchDog.register(20);

            while (runLoop) {
//                if(!myWatchDog.update()){
//                    myWatchDog.init();
//                    myWatchDog.register(GetEnvDataRunnable.class.getSimpleName(), 20);
//                }
                try {
                    //Object tagHistoryInfo = eventBuffer.take();
                    Object tagHistoryInfo = eventBuffer.poll(10, TimeUnit.SECONDS);


                    if (!runLoop) {
                        break;
                    }

                    if(tagHistoryInfo == null){
                        continue;
                    }
                    parse(tagHistoryInfo);
                } catch (InterruptedException e) {
                    LOG.error("unable to take data:" + e.getMessage());
                    if (!runLoop) {
                        break;
                    }
                }

                if (!runLoop) {
                    break;
                }

                try {

                    Thread.sleep(DiagnosticsConfig.DIAGD_EVENT_PULL_DELAY);
                } catch (InterruptedException e) {

                    LOG.error("sleep was interrupted:" + e);
                    if (!runLoop) {
                        break;
                    }
                }
            }

            LOG.info("GetEnvDataRunnable Thread stop");
        }


    }

    public void putInEventBuffer(AlarmValueHistoryInfo hist) {
        LOG.debug("putInEventBuffer history");
        try {
            this.eventBuffer.put(hist);
        } catch (InterruptedException e) {

            LOG.error("putInEventBuffer interrupted " + e);
        }
    }

    private void parse(Object take) {

        if(take instanceof AlarmEnvironmentDataRequester){
            //count
            AlarmEnvironmentDataRequester webElement = (AlarmEnvironmentDataRequester)take;

            if(webElement.getRequestCount() <=0){
                return;
            };

            webElement.start();
            return;

        }

        if(take instanceof AlarmValueHistoryInfo){
            //find categories for this event
            AlarmValueHistoryInfo alarmValueHistoryInfo = (AlarmValueHistoryInfo) take;
            //send event to add new history info
            fireEvent(new DCDPullerEvent(alarmValueHistoryInfo));

            List<AlarmCategory> myList = WebAppUtils.getInstance().getCategoriesForEvent(alarmValueHistoryInfo);

            if(myList.size() == 0){
                if(alarmValueHistoryInfo.getMyTag() != null){
                    LOG.info("there is no category for Event DataTag "+ alarmValueHistoryInfo.getMyTag().getName());
                } else {
                    LOG.info("AlarmCategory is empty and Tag is null");

                }

                return;
            }

            //send frame for this event
            for (AlarmCategory cat : myList){
                //send request
                //internal start timer to send get env data
                AlarmEnvironmentDataRequester webElement = new AlarmEnvironmentDataRequester(alarmValueHistoryInfo, cat);
                //store the history data for further use
                List<AlarmEnvironmentDataRequester> myRequestList = alarmEnvironmentDataRequesterMap.get(alarmValueHistoryInfo.getId());
                if(myRequestList == null){
                    myRequestList = new ArrayList<>();
                    alarmEnvironmentDataRequesterMap.put(alarmValueHistoryInfo.getId(), myRequestList);
                }

                myRequestList.add(webElement);
                webElement.start();
            }

            return;
        }
    }

    void fireEvent(final DCDPullerEvent eventPullerEvent){

        String vID = eventPullerEvent.getAlarmValueHistoryInfo().getVehicleId();
        Set<DCDPullerEventHandler> mySet = this.
                dcdEventPullerHandlerListeners.get(vID);
        if(mySet == null){
            LOG.debug("no CDPullerEventHandler listening");
            return;
        }

        LOG.debug(String.format("we have %d listeners for vehicle %s", mySet.size(), vID));
        for(final DCDPullerEventHandler handler : mySet){

            Runnable runnable = new Runnable() {
                public void run() {

                  handler.handleEvent(eventPullerEvent);

              }

        };
            new Thread(runnable).start();
         }
    }

    public void replaceListener(String oldVehicleId, String newVehicleId, DCDPullerEventHandler handler){

        if(oldVehicleId != null){
            Set<DCDPullerEventHandler> myOldSet = this.dcdEventPullerHandlerListeners.get(oldVehicleId);

            if(myOldSet != null){
                myOldSet.remove(handler);
            }
        }

        Set<DCDPullerEventHandler> myNewSet = this.dcdEventPullerHandlerListeners.get(oldVehicleId);
        if(myNewSet == null){
            myNewSet = new HashSet<>();
            this.dcdEventPullerHandlerListeners.put(newVehicleId, myNewSet);
        }

        myNewSet.add(handler);
        LOG.debug("listener added");

    }

    public void removeListener(String vehicleId, DCDPullerEventHandler handler){

        Set<DCDPullerEventHandler> mySet = this.dcdEventPullerHandlerListeners.get(vehicleId);

        if(mySet != null){
            mySet.remove(handler);
        }

        LOG.debug("listener removed");

    }



    public void restartSendEnvironmentData(AlarmEnvironmentDataRequester myAlarm) {


        putNextWithStatus(myAlarm, EventEnvDataStatus.GET_START);

    }



    public void removeSendEnvironmentData(AlarmEnvironmentDataRequester myAlarm) {

        Integer index = myAlarm.getId();

        myAlarm.setEnvDataStatus(EventEnvDataStatus.GET_END);

        List<AlarmEnvironmentDataRequester> myList = this.alarmEnvironmentDataRequesterMap.get(index);

        if(myList == null){
            return;
        }

        if(!myList.remove(myAlarm)){
            LOG.error("no AlarmEnvironmentDataRequester removed");
        }

        if(myList.size() <= 0){
            LOG.debug("remove list from map");
            this.alarmEnvironmentDataRequesterMap.remove(index);
        }
    }

    public void nextSendEnvironmentData(AlarmEnvironmentDataRequester myAlarm) {

        putNextWithStatus(myAlarm,EventEnvDataStatus.GET_NEXT);
    }

    private void putNextWithStatus(AlarmEnvironmentDataRequester myAlarm ,EventEnvDataStatus status ){

        if(myAlarm == null){
            LOG.error("AlarmEnvironmentDataRequester not found");
            return;
        }

        myAlarm.setEnvDataStatus(status);
        myAlarm.incrementPacketCount();

        try {
            LOG.debug(String.format("putInEventBuffer next history %d:%d PACKET %d",
                    myAlarm.getEventIndex(), myAlarm.getCategoryIndex(), myAlarm.getPacketCount()));
            this.eventBuffer.put(myAlarm);
        } catch (InterruptedException e) {

            LOG.error("restartSendEnvironmentData interrupted " + e);
        }
    }

    public AlarmEnvironmentDataRequester findAlarmRequester(Integer eventIndex, Integer categoryIndex) {

        for(List<AlarmEnvironmentDataRequester> eList : alarmEnvironmentDataRequesterMap.values()){

            for(AlarmEnvironmentDataRequester e : eList){
                if(e.getEventIndex().equals(eventIndex)){
                    if(e.getCategoryIndex().equals(categoryIndex)){
                        return e;
                    }
                }
            }
        }

        return null;
    }

    public List<AlarmEnvironmentDataRequester> findAlarmRequester(String guId) {

        List<AlarmEnvironmentDataRequester> myList = new ArrayList<>();

        for(List<AlarmEnvironmentDataRequester> eList : alarmEnvironmentDataRequesterMap.values()){

            for(AlarmEnvironmentDataRequester e : eList){
                if(e.getGuID().equals(guId)){
                    myList.add(e);
                }
            }
        }

        return myList;
    }

}
