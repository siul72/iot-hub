package co.luism.diagnostics.enterprise;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by luis on 14.11.14.
 */
@Entity
@Table(catalog = "ondiagnose")
public class HistoryAlarmTagValue extends TagValue {


    protected HistoryAlarmTagValue(){

    }

    public HistoryAlarmTagValue(String vehicleId, int tagId, Integer timeStamp, Integer milliSeconds, long value, double scale) {
        super(vehicleId, tagId, timeStamp, milliSeconds,value, scale);
    }
}
