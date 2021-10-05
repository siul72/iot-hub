package co.luism.lukisoftiot.interfaces;

import co.luism.lukisoftiot.common.GetEnvStatusEvent;

/**
 * Created by luis on 24.11.14.
 */
public interface IGetEnvStatusEventHandler {
    public void handleEnvStatus(GetEnvStatusEvent eo);
}
