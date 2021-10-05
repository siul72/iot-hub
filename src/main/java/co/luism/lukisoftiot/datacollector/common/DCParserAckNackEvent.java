package co.luism.lukisoftiot.datacollector.common;

import co.luism.lukisoftiot.common.FunctionEnum;


import java.util.EventObject;

/**
 * Created by luis on 27.11.14.
 */
public class DCParserAckNackEvent extends EventObject {

    private final String vehicleId;
    private final FunctionEnum sourceMessage;
    private final FunctionEnum result;

    private static final long serialVersionUID = 2L;

    public DCParserAckNackEvent(String vehicleId, FunctionEnum sourceMessage, FunctionEnum result) {
        super(vehicleId);
        this.vehicleId = vehicleId;
        this.sourceMessage = sourceMessage;
        this.result = result;

    }

    public String getVehicleId() {
        return vehicleId;
    }

    public FunctionEnum getSourceMessage() {
        return sourceMessage;
    }

    public FunctionEnum getResult() {
        return result;
    }
}
