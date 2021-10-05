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

package co.luism.lukisoftiot.webapputils;

import co.luism.lukisoftiot.common.WatchDogClient;

import co.luism.lukisoftiot.enterprise.SnapShotAlarmTagValue;
import co.luism.lukisoftiot.enterprise.Vehicle;
import co.luism.lukisoftiot.interfaces.IVehicleEventHandler;
import co.luism.lukisoftiot.common.DiagnosticsConfig;
import co.luism.lukisoftiot.common.EventTypeEnum;
import co.luism.lukisoftiot.common.VehicleEvent;
import co.luism.lukisoftiot.common.VehicleSyncStatusEnum;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class VehicleDataSynchronization implements IVehicleEventHandler {

    private static final Logger LOG = Logger.getLogger(VehicleDataSynchronization.class);
    private final BlockingQueue<Vehicle> buffer = new LinkedBlockingQueue<>(50);
    private boolean runLoop = true;
    private final LoaderRunnable myLoader = new LoaderRunnable();
    public static final VehicleDataSynchronization instance = new VehicleDataSynchronization();
    public static VehicleDataSynchronization getInstance(){
        return instance;
    }

    VehicleDataSynchronization(){

    }

    public void init(){
        Thread myThread = new Thread(myLoader);
        myThread.setName(VehicleDataSynchronization.class.getSimpleName());
        myThread.start();
    }


    @Override
    public void handleVehicleEvent(VehicleEvent vehicleEvent) {
        //parse it
        //put it on the buffer
        try {
            buffer.put(vehicleEvent.getCurrentVehicle());
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }

    }

    private void process(Vehicle vehicle) {

        switch (vehicle.getSyncStatus()){


            case SYNC_STATUS_DB_TO_VEHICLE_OK:
                syncAlarmDataFromVehicle(vehicle);
            break;

            default:
                //if offline/online need to be sync
                syncAlarmDataFromDataBase(vehicle);
            break;

        }


    }

    private void syncAlarmDataFromVehicle(Vehicle vehicle) {
        vehicle.setSyncStatus(VehicleSyncStatusEnum.SYNC_STATUS_RUNNING);
        //vehicle.resetAllOldSnapShotAlarmValues();
        LOG.debug(String.format("sync finish (V->DB) for %s", vehicle.getVehicleId()));
        vehicle.setSyncStatus(VehicleSyncStatusEnum.SYNC_STATUS_ALL_SYNC);
        WebAppUtils.getInstance().sendRefreshAlarmsEvent(vehicle);
    }

    private void syncAlarmDataFromDataBase(Vehicle vehicle) {

        vehicle.setSyncStatus(VehicleSyncStatusEnum.SYNC_STATUS_RUNNING);
        Map<String, Object> myRestrictions = new HashMap<>();
        myRestrictions.put("vehicleId", vehicle.getVehicleId());

        //list of all snapshot alarms
        List<SnapShotAlarmTagValue> alarmDataList = SnapShotAlarmTagValue.getList(SnapShotAlarmTagValue.class, myRestrictions);

        for(SnapShotAlarmTagValue alarmTagValue : alarmDataList){

            if(vehicle.getAlarmTag(alarmTagValue.getTagId())== null){

                if(alarmTagValue.isAck()){
                    continue;
                }
                vehicle.putSnapShotValue(EventTypeEnum.TAG_DATA_TYPE_EVENT, alarmTagValue, false);
            }
        }

        LOG.debug(String.format("sync finish (DB->V) for %s", vehicle.getVehicleId()));
        vehicle.setSyncStatus(VehicleSyncStatusEnum.SYNC_STATUS_DB_TO_VEHICLE_OK);
    }

    public void close() {
        runLoop = false;
        try {
            buffer.put(new Vehicle());
        } catch (InterruptedException e) {


            LOG.error("unable to put exit string:" + e.getMessage());
        }
    }

    class LoaderRunnable implements Runnable{
        private WatchDogClient myWatchDog = new WatchDogClient(VehicleDataSynchronization.class.getSimpleName());
        @Override
        public void run() {
            LOG.info(VehicleDataSynchronization.class.getSimpleName() + " Thread started");
            myWatchDog.init();
            myWatchDog.register(20);
            runLoop = true;
            while (runLoop) {
                Vehicle vehicle;
                if(!myWatchDog.update()){
                    myWatchDog.init();
                    myWatchDog.register(20);
                }
                try {
                    //vehicle = buffer.take();
                    vehicle = buffer.poll(10, TimeUnit.SECONDS);

                    if (!runLoop) {
                        break;
                    }

                    if(vehicle == null){
                        continue;
                    }

                    process(vehicle);

                } catch (InterruptedException e) {

                    LOG.error("unable to take data:" + e.getMessage());
                    if (!runLoop) {
                        break;
                    }
                }


                try {
                    Thread.sleep(DiagnosticsConfig.WEB_V_LOADER_DELAY);
                } catch (InterruptedException e) {

                    LOG.error("sleep was interrupted:" + e.getMessage());
                    if (!runLoop) {
                        break;
                    }
                }
            }
            LOG.info(VehicleDataSynchronization.class.getSimpleName() + " Thread stop");
        }
    }
}
