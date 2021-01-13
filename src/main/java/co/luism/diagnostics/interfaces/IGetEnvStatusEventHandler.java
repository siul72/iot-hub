package co.luism.diagnostics.interfaces;

import co.luism.diagnostics.common.GetEnvStatusEvent;

/**
 * Created by luis on 24.11.14.
 */
public interface IGetEnvStatusEventHandler {
    public void handleEnvStatus(GetEnvStatusEvent eo);
}
