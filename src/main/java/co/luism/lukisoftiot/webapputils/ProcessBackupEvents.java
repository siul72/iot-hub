package co.luism.lukisoftiot.webapputils;

import co.luism.lukisoftiot.datacollector.DataCollectorDiagnosticEventPuller;
import co.luism.lukisoftiot.datacollector.DataCollectorSocket;
import co.luism.lukisoftiot.enterprise.AlarmValueHistoryInfo;
import co.luism.lukisoftiot.enterprise.SnapShotAlarmTagValue;
import co.luism.lukisoftiot.enterprise.Vehicle;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luis on 17.02.15.
 */
public class ProcessBackupEvents implements Runnable{

    private static final Logger LOG = Logger.getLogger(ProcessBackupEvents.class);
    private final Vehicle vehicle;

    public ProcessBackupEvents(Vehicle v) {
        this.vehicle = v;
    }

    @Override
    public void run() {

        LOG.info(vehicle.getVehicleId() + " Start BACKUP... ");

        doJob();

        LOG.info("... End BACKUP " + vehicle.getVehicleId());

    }

    private synchronized void doJob() {

        //CREATE TABLE recipes_new LIKE production.recipes; INSERT recipes_new SELECT * FROM production.recipes;
        //timestamp and configuration
        //we need to stop any retrieve envdata for the vehicle
        String guIdConnection = DataCollectorSocket.getInstance().getGUID(vehicle.getVehicleId());
        if(guIdConnection == null){
            return;
        }

        DataCollectorDiagnosticEventPuller.getInstance().stopPulling(guIdConnection);

        Map<String, Object> myRestrictions = new HashMap();
        myRestrictions.put("vehicleId", vehicle.getVehicleId());
        List<AlarmValueHistoryInfo> myList = AlarmValueHistoryInfo.getList(AlarmValueHistoryInfo.class, myRestrictions);

        for(AlarmValueHistoryInfo alarmValue : myList){
            alarmValue.setAck(true);
            alarmValue.update();

            //get related env data
        }

        //get snapshot values for alarms mark them as ack and remove them from snapshot

        List<SnapShotAlarmTagValue> myAlarmList = vehicle.getActiveAlarms();

        if(myAlarmList.size() > 0){

            for(SnapShotAlarmTagValue alarm : myAlarmList){
                alarm.setAck(true);
                alarm.update();
            }

            vehicle.clearAlarmList();

        }



    }
}
