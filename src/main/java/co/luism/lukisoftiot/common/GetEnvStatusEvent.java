package co.luism.lukisoftiot.common;

import java.util.EventObject;

/**
 * Created by luis on 24.11.14.
 */
public class GetEnvStatusEvent extends EventObject{

    private final EventEnvDataStatus status;
    private final Integer historyId;
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public GetEnvStatusEvent(EventEnvDataStatus source, Integer historyId) {
        super(source);
        this.status = source;
        this.historyId = historyId;
    }

    public EventEnvDataStatus getStatus() {
        return status;
    }

    public Integer getHistoryId() {
        return historyId;
    }
}
