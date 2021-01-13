package co.luism.datacollector.common;

import co.luism.ksoft.iot.utils.common.CognitioBufferPosition;
import co.luism.ksoft.iot.utils.common.CognitioUtilsSettings;
import co.luism.diagnostics.common.DateTimeUtils;
import co.luism.diagnostics.common.DiagnosticsEvent;
import co.luism.diagnostics.common.EventTypeEnum;
import co.luism.diagnostics.common.ReturnCode;

import co.luism.diagnostics.enterprise.*;
import co.luism.diagnostics.interfaces.IDiagnosticsEventHandler;
import co.luism.diagnostics.webmanager.WebManager;
import co.luism.diagnostics.webmanager.WebManagerFacade;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static co.luism.ksoft.iot.utils.common.CognitioUtils.byteArrayToInt2B;
import static co.luism.ksoft.iot.utils.common.CognitioUtils.byteArrayToInt4B;

/**
 * Created by luis on 02.02.15.
 */
public class DCImport {

    private static final Logger LOG = Logger.getLogger(DCImport.class);

    private Map<String, File> zipFiles;
    private Vehicle vehicle;
    private String vehicleId;
    private String projectCode;
    private String importStamp;
    private IDiagnosticsEventHandler listener;

    public ReturnCode init(Vehicle v, IDiagnosticsEventHandler listener,File zipFile){

        this.listener = listener;
        importStamp = "import:" + DateTimeUtils.getCurrentTimeStringUtc();
        zipFiles = unzipFiles(zipFile, zipFile.getAbsolutePath());

        StringBuilder sb = new StringBuilder();
        sb.append("Zip Contents:");
        for(String name : zipFiles.keySet()){
            sb.append(name + ";");
        }
        LOG.info(sb.toString());


        sb.setLength(0);
        ReturnCode ret;
        ret = readLineProperty(zipFiles.get(CognitioUtilsSettings.FILE_VEHICLE), sb);

        if(ret != ReturnCode.RET_OK){
            return ret;
        }

        vehicleId = sb.toString();
        sb.setLength(0);

        ret = readLineProperty(zipFiles.get(CognitioUtilsSettings.FILE_PROPERTY), sb);
        if(ret != ReturnCode.RET_OK){
            return ret;
        }

        projectCode = sb.toString();
        sb.setLength(0);

        ret = checkVehicleConfiguration(v, vehicleId, projectCode);
        if(ret != ReturnCode.RET_OK){
            return ret;
        }

        return ReturnCode.RET_OK;
    }



    private ReturnCode readLineProperty(File f, StringBuilder bf){
        BufferedReader ins = null;

        if(f == null){
            return ReturnCode.IMPORT_VEHICLE_ID_NOT_FOUND;
        }

        try {
            ins = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            LOG.error(e);
            return ReturnCode.IMPORT_VEHICLE_ID_NOT_FOUND;
        }

        try {

            bf.append(ins.readLine());
        } catch (IOException e) {
            LOG.error(e);

            try {
                ins.close();
            } catch (IOException e1) {
                LOG.error(e1);
            }

            return ReturnCode.IMPORT_VEHICLE_ID_NOT_READ;
        }


        try {
            ins.close();
        } catch (IOException e) {
            LOG.error(e);
            return ReturnCode.IMPORT_IO_ERROR;
        }

        return ReturnCode.RET_OK;

    }

    private ReturnCode checkVehicleConfiguration(Vehicle v, String vehicleId, String projectCode) {


        if(v == null){
            LOG.info("Vehicle is null");
            return ReturnCode.IMPORT_VEHICLE_NOT_FOUND;
        }

        if(vehicleId == null){
            LOG.info("vehicleId is null");
            return ReturnCode.IMPORT_VEHICLE_ID_NOT_MATCH;
        }

        if(!v.getVehicleId().equals(vehicleId)){
            LOG.info(String.format("Expected vid %s, found %s", v.getVehicleId(),
                    vehicleId));
            return ReturnCode.IMPORT_VEHICLE_ID_NOT_MATCH;
        }

        String vCode = v.getMyFleet().getMyConfiguration().getProjectCode();

        if(projectCode == null || vCode == null){
            LOG.info("projectCode  is null");
            return ReturnCode.IMPORT_PROJECT_CODE_NOT_MATCH;
        }

        if(!projectCode.equals(vCode)){
            LOG.info(String.format("Expected config %s, found %s", vCode,
                    projectCode));
            return ReturnCode.IMPORT_PROJECT_CODE_NOT_MATCH;
        }

        vehicle =v;

        return ReturnCode.RET_OK;

    }

    private Map<String, File> unzipFiles(File zipFile, String outputFolder)
    {
        Map<String, File> mapExtractedFiles = new HashMap<>();

        byte[] buffer = new byte[1024];

        try{

            int p = outputFolder.lastIndexOf('.');
            if(p > 0){
                outputFolder = outputFolder.substring(0, p);
            }


            //create output directory is not exists
            File folder = new File(outputFolder);
            if(!folder.exists()){
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while(ze!=null){

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                mapExtractedFiles.put(fileName, newFile);
                LOG.debug("file unzip : "+ newFile.getAbsoluteFile());
                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);


                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            LOG.debug("Done unzip file");

        }catch(IOException ex){
            LOG.error(ex);
        }

        return mapExtractedFiles;

    }

    public Map<String, File> getZipFiles() {
        return zipFiles;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public ReturnCode importAlarmData() {

        fireEvent(new DiagnosticsEvent(this), EventTypeEnum.PROGRESS_IMPORT_ALARM_DATA, 0.0f);

        File f = zipFiles.get(CognitioUtilsSettings.FILE_ERR);
        ByteArrayInputStream byteArrayInputStream = null;

        if(f == null){
            return ReturnCode.IMPORT_ERROR_FILE_NOT_FOUND;
        }

        try {
            byteArrayInputStream = new ByteArrayInputStream(FileUtils.readFileToByteArray(f));
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        if(byteArrayInputStream == null){
            return ReturnCode.IMPORT_IO_ERROR;
        }

        int amountToRead = CognitioBufferPosition.EVENT_SIZE;
        byte[] bufferRead = new byte[amountToRead];
        int pointerRead =0;
        int iByteOrder = 0;
        byteArrayInputStream.reset();
        long total = byteArrayInputStream.available();

        fireEvent(new DiagnosticsEvent(this), EventTypeEnum.PROGRESS_IMPORT_ALARM_DATA, 0.0f);

        while(pointerRead <  total){

            byteArrayInputStream.read(bufferRead, 0, amountToRead);
            int eventIndex = (int)byteArrayToInt4B( bufferRead, CognitioBufferPosition.EVENT_INDEX_POS, iByteOrder);
            int startTimeStamp_s = (int)byteArrayToInt4B( bufferRead, CognitioBufferPosition.EVENT_START_TIME_SECONDS_POS, iByteOrder);
            int endTimeStamp_s = (int)byteArrayToInt4B( bufferRead, CognitioBufferPosition.EVENT_END_TIME_S_POS, iByteOrder);
            int eventCounter = (int)byteArrayToInt4B( bufferRead, CognitioBufferPosition.EVENT_COUNT_POS, iByteOrder);
            int startTimeStamp_ms = byteArrayToInt2B( bufferRead, CognitioBufferPosition.EVENT_START_TIME_MSECONDS_POS, iByteOrder);
            int endTimeStamp_ms = byteArrayToInt2B( bufferRead, CognitioBufferPosition.EVENT_END_TIME_MSECONDS_POS, iByteOrder);
            int eventCode = byteArrayToInt2B( bufferRead, CognitioBufferPosition.EVENT_CODE_POS, iByteOrder);
            int statusInfo = byteArrayToInt2B( bufferRead, CognitioBufferPosition.EVENT_STATUS_POS, iByteOrder);

            if(startTimeStamp_s == 0){
                break;
            }

            pointerRead = pointerRead + amountToRead;
            Float progress = ((float)pointerRead)/total;
            fireEvent(new DiagnosticsEvent(this), EventTypeEnum.PROGRESS_IMPORT_ALARM_DATA, progress);

            //find if event exists
            DataTag tag = WebManagerFacade.getInstance().getTagBySourceId(EventTypeEnum.TAG_DATA_TYPE_EVENT,vehicle.getConfigurationId(), eventCode);

            if(tag == null){
                LOG.error(String.format("no DataTag found for eventCode %d ", eventCode));
                continue;
            }

            //store the History Data
            AlarmValueHistoryInfo hist;
            Map<String, Object> myRestriction = new HashMap<>();
            myRestriction.put("eventIndex", eventIndex);
            myRestriction.put("tagId", tag.getTagId());
            myRestriction.put("vehicleId", this.vehicleId);
            myRestriction.put("startTimeStamp", startTimeStamp_s);
            myRestriction.put("startTSMilliseconds", startTimeStamp_ms);
            //readback
            hist = AlarmValueHistoryInfo.read(AlarmValueHistoryInfo.class, myRestriction);

            if(hist != null){
                LOG.warn(String.format("A value for the alarm %d:%s:%s %d:%d is already in the database",
                        eventIndex, tag.getTagId(), this.vehicleId, startTimeStamp_s,
                        startTimeStamp_ms));
                continue;
            }

            hist =  new AlarmValueHistoryInfo(tag.getTagId(), this.vehicleId, eventIndex,
                    startTimeStamp_s, startTimeStamp_ms, endTimeStamp_s, endTimeStamp_ms,
                    eventCounter, eventCode, statusInfo, tag);


            hist.setUpdateBy(importStamp);

            hist.create();
            LOG.warn(String.format("New value imported for the alarm %d:%s:%s %d:%d",
                    eventIndex, tag.getTagId(), this.vehicleId, startTimeStamp_s,
                    startTimeStamp_ms));

        }


        return ReturnCode.RET_OK;
    }

    private void fireEvent(final DiagnosticsEvent eo, final EventTypeEnum eventType, final Float value ) {
        if(listener != null){

            Runnable runnable = new Runnable() {
                public void run() {
                    eo.setTagType(eventType);
                    eo.setValue(value);
                    listener.handleDiagnosticsEvent(eo);

                }

            };

            new Thread(runnable).start();

        }
    }

    public ReturnCode importEnvironmentData() {

        ByteArrayInputStream byteArrayInputStream = null;

        for(File f : zipFiles.values()){
            if(!f.getName().endsWith(CognitioUtilsSettings.END_UMF)) {
                continue;
            }

            fireEvent(new DiagnosticsEvent(this), EventTypeEnum.PROGRESS_IMPORT_ENV_DATA, 0.0f);

            int iStart = f.getName().indexOf("_");
            int iEnde = f.getName().indexOf(".");
            int categoryIndex;

            try {
                categoryIndex = Integer.parseInt(f.getName().substring(iStart + 1, iEnde));
            } catch (NumberFormatException ex) {
                LOG.error(String.format("invalid category for file %s", f.getName()));
                continue;
            }

            // Grösse einer Zeile berechnen
            AlarmCategory tempKategorie = WebManagerFacade.getInstance().getCategory(categoryIndex);
            if(tempKategorie == null){
                LOG.error("AlarmCategory not found");
                continue;
            }
            int categorySize = tempKategorie.getSizeInBytes();

            try {
                byteArrayInputStream = new ByteArrayInputStream(FileUtils.readFileToByteArray(f));
            } catch (IOException e) {
                LOG.error(e);
                continue;
            }

            if(byteArrayInputStream == null){
                return ReturnCode.IMPORT_IO_ERROR;
            }

            int amountToRead = CognitioBufferPosition.ENVIRONMENT_DATA_SIZE;
            byte[] bufferHeadRead = new byte[amountToRead];
            byte[] bufferDataRead = new byte[categorySize];
            int pointerRead = 0;
            int iByteOrder = 0;
            int total = byteArrayInputStream.available();

            byteArrayInputStream.reset();
            while(pointerRead < total){

                byteArrayInputStream.read(bufferHeadRead, 0, amountToRead);
                pointerRead = pointerRead + amountToRead;
                int eventIndex = (int)byteArrayToInt4B(bufferHeadRead, CognitioBufferPosition.ENVIRONMENT_DATA_EVENT_INDEX_POS, iByteOrder);
                //Buffer index not read
                int timeStampSeconds = (int)byteArrayToInt4B(bufferHeadRead, CognitioBufferPosition.ENVIRONMENT_DATA_TIMESTAMP_SECONDS_POS, iByteOrder);
                int timeStampMilliSeconds = byteArrayToInt2B(bufferHeadRead, CognitioBufferPosition.ENVIRONMENT_DATA_TIMESTAMP_MSECONDS_POS , iByteOrder);

                byteArrayInputStream.read(bufferDataRead, 0, categorySize);
                pointerRead = pointerRead + categorySize;
                Float progress = ((float)pointerRead)/total;
                fireEvent(new DiagnosticsEvent(this), EventTypeEnum.PROGRESS_IMPORT_ALARM_DATA, progress);

                //get Alarm by Event Index
                AlarmValueHistoryInfo myHistoryAlarm = WebManager.getInstance().getAlarmValueByIndex(eventIndex, this.vehicle.getVehicleId());
                AlarmEnvironmentData environmentData;
                Map<String, Object> myRestriction = new HashMap<>();
                myRestriction.put("alarmTagHistoryInfoId", myHistoryAlarm.getId());
                myRestriction.put("eventIndex", myHistoryAlarm.getEventIndex());
                myRestriction.put("categoryIndex", tempKategorie.getCategoryIndex());
                myRestriction.put("timeStampSeconds", timeStampSeconds);
                myRestriction.put("timeStampMilliSeconds", timeStampMilliSeconds);
                //read back
                environmentData = AlarmEnvironmentData.read(AlarmEnvironmentData.class, myRestriction);

                if(environmentData != null){
                    LOG.warn(String.format("A value for the environment data %d:%d:%d %d:%d is already in the database",
                            myHistoryAlarm.getId(), myHistoryAlarm.getEventIndex(),
                            tempKategorie.getCategoryIndex(), timeStampSeconds,
                            timeStampMilliSeconds));
                    continue;
                }

                 environmentData = new AlarmEnvironmentData(myHistoryAlarm.getId(),myHistoryAlarm.getEventIndex(),
                        tempKategorie.getCategoryIndex(),
                        timeStampSeconds, timeStampMilliSeconds, new String(Hex.encodeHex(bufferDataRead)));

                environmentData.setUpdateBy(importStamp);

                try {
                    environmentData.create();
                } catch (Exception ex){
                    LOG.error(ex);
                }


            }

        }

        return ReturnCode.RET_OK;

    }
}
