package co.luism.diagnostics.enterprise;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by luis on 18.08.15.
 */
@Entity
@Table(catalog = "ondiagnose")
public class HistoryGenericValue extends TagValue {


    protected HistoryGenericValue(){

    }

    public HistoryGenericValue(String vehicleId, int tagId, Integer timeStamp, Integer milliSeconds, long value, double scale) {
        super(vehicleId, tagId, timeStamp, milliSeconds,value, scale);
    }

    public HistoryGenericValue(SnapShotGenericValue snapShotGenericValue) {
        this(snapShotGenericValue.getVehicleId(), snapShotGenericValue.getTagId(), snapShotGenericValue.getTimeStamp(),
                snapShotGenericValue.getMilliSeconds(), snapShotGenericValue.getValue(), snapShotGenericValue.getScale());
    }
}
