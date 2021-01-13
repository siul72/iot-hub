package co.luism.datacollector.common;

import co.luism.diagnostics.enterprise.AlarmValueHistoryInfo;

import java.util.EventObject;

/**
 * Created by luis on 24.11.14.
 */
public class DCDPullerEvent extends EventObject{

    private final AlarmValueHistoryInfo alarmValueHistoryInfo;
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public DCDPullerEvent(AlarmValueHistoryInfo source) {
        super(source);
        this.alarmValueHistoryInfo = source;
    }

    public AlarmValueHistoryInfo getAlarmValueHistoryInfo() {
        return alarmValueHistoryInfo;
    }
}
