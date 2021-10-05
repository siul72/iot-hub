package co.luism.lukisoftiot.enterprise;

import co.luism.lukisoftiot.common.AlarmSyncStatus;

import javax.persistence.*;


/**
 * Created by luis on 29.10.14.
 */

@Entity
@Table(catalog = "lukiiot")
public class SnapShotAlarmTagValue extends TagValue {

    @Transient
    private AlarmSyncStatus alarmSyncStatus;

    protected SnapShotAlarmTagValue(){
        this.alarmSyncStatus = AlarmSyncStatus.ALARM_SYNC_OLD;

    }

    public SnapShotAlarmTagValue(String vehicleId, int tagId, Integer timeStamp, Integer milliSeconds, long value, double scale, boolean update) {
        super(vehicleId, tagId, timeStamp, milliSeconds,value, scale);

        if(update){
            this.alarmSyncStatus = AlarmSyncStatus.ALARM_SYNC_FRESH;
        } else {
            this.alarmSyncStatus = AlarmSyncStatus.ALARM_SYNC_OLD;
        }

    }

    public AlarmSyncStatus getAlarmSyncStatus() {
        return alarmSyncStatus;
    }

    public void setAlarmSyncStatus(AlarmSyncStatus alarmSyncStatus) {
        this.alarmSyncStatus = alarmSyncStatus;
    }
}
