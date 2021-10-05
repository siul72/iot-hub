package co.luism.lukisoftiot.enterprise;


import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by luis on 26.01.15.
 */
@Entity
@Table(catalog = "lukiiot")
public class SnapShotGenericValue extends TagValue {

    protected SnapShotGenericValue(){

    }

    public SnapShotGenericValue(String vehicleId, Integer id, Integer timeStamp, Integer milliSeconds, Long value, double scale) {
        super(vehicleId, id, timeStamp, milliSeconds,value, scale);
    }

    @Override
    public void setUpdateBy(String updateBy) {

    }
}
