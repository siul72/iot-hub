package co.luism.datacollector.common;

import co.luism.diagnostics.common.ReturnCode;
import co.luism.diagnostics.enterprise.Vehicle;
import co.luism.diagnostics.interfaces.IDiagnosticsEventHandler;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by luis on 02.02.15.
 */
public class DataCollectorImportUtils {

    private static final Logger LOG = Logger.getLogger(DataCollectorImportUtils.class);

    public static ReturnCode importAlarms(Vehicle vehicle, IDiagnosticsEventHandler listener, File zipFile) {

        DCImport myImport = new DCImport();
        ReturnCode ret = myImport.init(vehicle, listener, zipFile);
        if (ret != ReturnCode.RET_OK) {
            return ret;
        }

        myImport.importAlarmData();
        myImport.importEnvironmentData();

        return ReturnCode.RET_OK;

    }


}
