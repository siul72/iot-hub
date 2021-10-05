package co.luism.lukisoftiot.enterprise;

import co.luism.lukisoftiot.common.EventTypeEnum;

/**
 * Created by luis on 13.11.14.
 */
public class GenericTagValue extends TagValue {

    private EventTypeEnum type;

    protected GenericTagValue(EventTypeEnum type){

        this.type = type;
    }

    public GenericTagValue(EventTypeEnum type, String vehicleId, int tagId, Integer timeStamp, Integer TSMilliseconds, long value, double scale) {
        super(vehicleId, tagId, timeStamp, TSMilliseconds,value, scale);
        this.type = type;
    }
}

