package co.luism.lukisoftiot.datacollector.common;

/**
 * Created by luis on 03.02.15.
 */
public class DataScannerUpdateStatus {

    public boolean newData = false;
    public boolean updateData = false;
    public boolean updatePosition = false;

    public void clear() {
        newData = false;
        updateData = false;
        updatePosition = false;
    }
}
