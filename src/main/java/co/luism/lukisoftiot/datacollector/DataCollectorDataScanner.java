/*
  ____        _ _ _                   _____           _
 |  __ \     (_) | |                 / ____|         | |
 | |__) |__ _ _| | |_ ___  ___      | (___  _   _ ___| |_ ___ _ __ ___  ___
 |  _  // _` | | | __/ _ \/ __|      \___ \| | | / __| __/ _ \ '_ ` _ \/ __|
 | | \ \ (_| | | | ||  __/ (__       ____) | |_| \__ \ ||  __/ | | | | \__ \
 |_|  \_\__,_|_|_|\__\___|\___|     |_____/ \__, |___/\__\___|_| |_| |_|___/
                                            __/ /
 Railtec Systems GmbH                      |___/
 6052 Hergiswil

 SVN file informations:
 Subversion Revision $Rev: $
 Date $Date: $
 Commmited by $Author: $
*/

package co.luism.lukisoftiot.datacollector;

import co.luism.lukisoftiot.common.WatchDogClient;
import co.luism.lukisoftiot.datacollector.common.DataCollectorClient;
import co.luism.lukisoftiot.datacollector.common.DataScannerUpdateStatus;
import co.luism.lukisoftiot.datacollector.messages.DCLifeSignParamEnum;
import co.luism.lukisoftiot.common.DiagnosticsEvent;
import co.luism.lukisoftiot.common.EventTypeEnum;

import co.luism.lukisoftiot.interfaces.IDiagnosticsEventHandler;
import co.luism.lukisoftiot.webapputils.WebAppUtils;
import co.luism.lukisoftiot.webapputils.WebManagerFacade;
import co.luism.lukisoftiot.enterprise.*;
import org.apache.log4j.Logger;
import org.hibernate.StaleStateException;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataCollectorDataScanner implements Runnable {

    private static final Logger LOG = Logger.getLogger(DataCollectorDataScanner.class);
    private static boolean runLoop;
    private static final List<DataScanCollector> dataScanCollectorList = new CopyOnWriteArrayList<>();
    private static final Map<String, Map<Integer, SnapShotGenericValue>> genericValuesMapList = new HashMap<>();
    public static final DataCollectorDataScanner instance = new DataCollectorDataScanner();
    public static DataCollectorDataScanner getInstance(){
        return instance;
    }
    private final Set<IDiagnosticsEventHandler> listeners = new HashSet<>();
    private final Map<DCLifeSignParamEnum, Integer> positionTagIdMap = new HashMap<>();
    private final Map<Integer, DCLifeSignParamEnum> tagIdPositionMap = new HashMap<>();
    private final String name = DataCollectorDataScanner.class.getSimpleName();
    private WatchDogClient myWatchDog = new WatchDogClient(name);

    DataCollectorDataScanner(){

    }

    @Override
    public void run() {

        runLoop = true;
        LOG.info(String.format("Started DataCollectorDataScanner with %d scan classes", dataScanCollectorList.size()));
        listeners.clear();
        myWatchDog.init();
        myWatchDog.register(5);
        while (runLoop){

            if(!myWatchDog.update()){
                myWatchDog.init();
                myWatchDog.register(5);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.error(e);
                if(!runLoop){
                    break;
                }
            }

            //LOG.debug("time to scan");
            //LOG.debug(String.format("number of scans %d %s", dataScanCollectorList.size(), dataScanCollectorList));
            for(DataScanCollector dataScanCollector : dataScanCollectorList){

                if(!runLoop){
                    break;
                }

                if (!dataScanCollector.getEnabled()){
                    continue;
                }

                Integer count = dataScanCollector.getCurrentCount();
                //LOG.debug(String.format("the count is %d", count));
                if(count > 0){
                    dataScanCollector.setCurrentCount(--count);
                    continue;
                }

                dataScanCollector.setCurrentCount(dataScanCollector.getPullTime());
                //LOG.debug(String.format("time to scan %s fleet", dataScanCollector.getMyFleet().getName()));

                //get data from vehicles by tag
                DataScannerUpdateStatus dataScannerUpdateStatus = new DataScannerUpdateStatus();
                Set<DataCollectorClient> myConnections = DataCollectorSocket.getInstance().getConnectedVehicleSet();

                if(!(myConnections.size() <= 0)){

                    for(DataCollectorClient dataCollectorClient : myConnections){
                        if(!runLoop){
                            break;
                        }
                        List<Integer> snapshotTagList = new ArrayList<>();
                        Vehicle vehicle = dataCollectorClient.getMyVehicle();

                        if(vehicle == null){
                            continue;
                        }

                        LOG.debug(String.format("Update online vehicle %s", vehicle.getVehicleId()));

                        try {

                            if(!vehicle.getFleetId().equals(dataScanCollector.getFleetId())){
                                continue;
                            }

                            snapshotTagList.addAll(vehicle.getSnapShotTagList());


                        } catch (NullPointerException ex){
                            LOG.error(ex);
                            continue;
                        }

                        Map<Integer, DataTag> myMap = dataScanCollector.getDataTagMap();

                        if(myMap.size() <= 0){
                            //LOG.debug("the tag map is empty");
                            continue;
                        }

                        Map<Integer, SnapShotGenericValue> currentMapList = genericValuesMapList.get(vehicle.getVehicleId());

                        LOG.debug(String.format("Number of dataScanCollector Tags %d", myMap.size()));
                        LOG.debug(String.format("Number of vehicle snapshot Tags %d", snapshotTagList.size()));
                        dataScannerUpdateStatus.clear();
                        for(Integer id : snapshotTagList){
                            if(!runLoop){
                                break;
                            }

                            DataTag dataTag = myMap.get(id);

                            if(dataTag == null){
                                LOG.debug(String.format("tag %d not found in dataScanCollector", id));
                                continue;
                            }

                            if(!id.equals(dataTag.getTagId())){
                                LOG.debug(String.format("snapshotId %d don't match dataTagId %d",
                                        id, dataTag.getTagId()));
                                continue;
                            }

                            createUpdateGenericValue(vehicle, id, currentMapList, dataScannerUpdateStatus);

                        }


                        if(dataScannerUpdateStatus.newData){
                            sendRefreshHistoryDataEvent(vehicle, EventTypeEnum.TAG_DATA_HISTORY_NEW_DATA);
                        } else {
                            if(dataScannerUpdateStatus.updateData){
                                sendRefreshHistoryDataEvent(vehicle, EventTypeEnum.TAG_DATA_HISTORY_REFRESH);
                            }
                        }

                        if(dataScannerUpdateStatus.updatePosition){
                            sendRefreshHistoryDataEvent(vehicle, EventTypeEnum.TAG_DATA_HISTORY_POSITION_REFRESH);
                            storeHistoryPosition(vehicle);

                        }
                    }
                } else {
                    //LOG.debug("connected vehicle list is empty");
                    LOG.debug(String.format("%s fleet as no vehicle connected", dataScanCollector.getMyFleet().getName()));

                }

                Collection<Vehicle> myOfflineVehicles = WebAppUtils.getInstance().getOfflineVehicleCollection();

                if(!(myOfflineVehicles.size() <= 0)) {
                    for (Vehicle vehicle : myOfflineVehicles) {

                        if (!vehicle.getFleetId().equals(dataScanCollector.getFleetId())) {
                            continue;
                        }
                        dataScannerUpdateStatus.clear();
                        Map<Integer, SnapShotGenericValue> currentMapList = genericValuesMapList.get(vehicle.getVehicleId());
                        Integer tagId = positionTagIdMap.get(DCLifeSignParamEnum.TRAIN_STATUS);
                        if (tagId == null) {
                            continue;
                        }

                        GenericTagValue tv = vehicle.getSnapShotValue(tagId);
                        SnapShotGenericValue snapShotGenericValue = currentMapList.get(tagId);

                        if (snapShotGenericValue != null && tv != null) {
                            if (tv.getValue().equals(snapShotGenericValue.getValue())) {

                                continue;
                            }
                        }

                        createUpdateGenericValue(vehicle, tagId, currentMapList, dataScannerUpdateStatus);


                        if (dataScannerUpdateStatus.newData) {
                            sendRefreshHistoryDataEvent(vehicle, EventTypeEnum.TAG_DATA_HISTORY_NEW_DATA);
                        } else {
                            if (dataScannerUpdateStatus.updateData) {
                                sendRefreshHistoryDataEvent(vehicle, EventTypeEnum.TAG_DATA_HISTORY_REFRESH);
                            }
                        }

                        if (dataScannerUpdateStatus.updatePosition) {
                            sendRefreshHistoryDataEvent(vehicle, EventTypeEnum.TAG_DATA_HISTORY_POSITION_REFRESH);
                        }

                    }
                } else {
                    LOG.debug("disconnected list is empty");
                }
             }
        }

        LOG.info("DataCollectorDataScanner Stopped");

    }

    private void storeHistoryPosition(Vehicle vehicle) {

        Map<DCLifeSignParamEnum, SnapShotGenericValue> myPositionValueList = DataCollectorDataScanner.getInstance().getPositionForVehicle(vehicle);
        SnapShotGenericValue latValue = myPositionValueList.get(DCLifeSignParamEnum.LATITUDE);

        if (latValue == null) {
            //LOG.warn("no value yet for Latitude");
            //setDefaultCenter();
            return;
        }

        SnapShotGenericValue longValue = myPositionValueList.get(DCLifeSignParamEnum.LONGITUDE);
        if (longValue == null) {
            //LOG.warn("no value yet for Longitude");
            //setDefaultCenter();
            return;
        }

        HistoryGenericValue historyGenericValue = new HistoryGenericValue(longValue);
        historyGenericValue.setValue(latValue.getValue() << 32 | longValue.getValue());
        historyGenericValue.create();

        SnapShotGenericValue status = myPositionValueList.get(DCLifeSignParamEnum.GPS_STATUS);

        if(status == null){
            return;
        }

        historyGenericValue = new HistoryGenericValue(longValue);
        historyGenericValue.setValue(status.getValue());
        historyGenericValue.create();

    }

    private void createUpdateGenericValue(Vehicle vehicle, Integer id, Map<Integer, SnapShotGenericValue> currentMapList, DataScannerUpdateStatus updateStatus){
        //get generic value
        GenericTagValue tv = vehicle.getSnapShotValue(id);

        if(tv == null){
            return;
        }

        SnapShotGenericValue snapShotGenericValue = currentMapList.get(id);

        if(snapShotGenericValue == null){
            //check first on DB
            Map<String, Object> myRestrictions = new HashMap<>();
            myRestrictions.put("vehicleId" , vehicle.getVehicleId());
            myRestrictions.put("tagId" , id);
            snapShotGenericValue = SnapShotGenericValue.read(SnapShotGenericValue.class, myRestrictions);

            if(snapShotGenericValue == null){
                snapShotGenericValue = new SnapShotGenericValue(vehicle.getVehicleId(), id,
                        tv.getTimeStamp(), tv.getMilliSeconds() , tv.getValue(), tv.getScale());
                snapShotGenericValue.create();

                //read back to get the id
                snapShotGenericValue = SnapShotGenericValue.read(SnapShotGenericValue.class, myRestrictions);
            }

            currentMapList.put(id, snapShotGenericValue);
            updateStatus.newData = true;
        } else {
            //else it exist must check for timestamp and bounds
            snapShotGenericValue.setTimeStamp(tv.getTimeStamp());
            snapShotGenericValue.setMilliSeconds(tv.getMilliSeconds());
            snapShotGenericValue.setValue(tv.getValue());
            try{
                snapShotGenericValue.update();

                //done update
                if(tagIdPositionMap.get(id) != null){
                    updateStatus.updatePosition = true;


                } else {
                    updateStatus.updateData = true;
                }
            } catch (StaleStateException ex){
                LOG.error(ex);
            }

        }

    }

    public void closeDown(){
        this.runLoop = false;
    }

    public void reload() {
        LOG.debug("reloading scan class");
        load();

    }

    private void load(){
        //get configuration
        List returnList = DataScanCollector.getList(DataScanCollector.class);
        dataScanCollectorList.clear();
        for(Object obj : returnList){
            if(obj instanceof DataScanCollector){
                dataScanCollectorList.add((DataScanCollector)obj);
            }
        }

        LOG.debug(String.format("number of scans %d %s", dataScanCollectorList.size(), dataScanCollectorList));
        for(DataScanCollector dataScanCollector : dataScanCollectorList){
            LOG.debug(String.format("DS for fleet %d with %d tags", dataScanCollector.getFleetId(), dataScanCollector.getDataTagMap().size()));
            StringBuilder b = new StringBuilder();
            for(Integer i : dataScanCollector.getDataTagMap().keySet()){
                b.append(String.format("%d,", i));
            }

            LOG.debug(String.format("List of tags %s", b.toString()));

            for(Vehicle v : dataScanCollector.getMyFleet().getVehicleMap().values()){
                Map<Integer, SnapShotGenericValue> myMap = genericValuesMapList.get(v.getVehicleId());
                if(myMap == null){
                    myMap = new HashMap<>();
                    genericValuesMapList.put(v.getVehicleId(), myMap);
                }
                myMap.clear();

                Map<String, Object> myRestrictions = new HashMap<>();
                myRestrictions.put("vehicleId" , v.getVehicleId());

                List<SnapShotGenericValue> myList = SnapShotGenericValue.getList(SnapShotGenericValue.class, myRestrictions);

                if(myList == null){
                    LOG.debug(String.format("No SnapShotGenericValues for %s", v.getVehicleId()));
                    continue;
                }

                if(myList.size() <= 0){
                    continue;
                }

                for(SnapShotGenericValue o : myList){

                    if(dataScanCollector.getDataTagMap().get(o.getTagId()) == null){
                        continue;
                    }

                    myMap.put(o.getTagId(), o);

                }

                LOG.debug(String.format("Add %d SnapShotGenericValues for %s", myMap.size(), v.getVehicleId()));
            }

        }
    }

    private void updatePositionTagMap(){
        this.positionTagIdMap.clear();
        this.tagIdPositionMap.clear();
        for(DCLifeSignParamEnum tagName : DCLifeSignParamEnum.values()){
            DataTag dataTag = WebManagerFacade.getInstance().getTagByName(EventTypeEnum.TAG_DATA_TYPE_SYSTEM, 0, tagName.name());
            if(dataTag == null){
                continue;
            }

            this.positionTagIdMap.put(tagName, dataTag.getTagId());
            this.tagIdPositionMap.put(dataTag.getTagId(), tagName);
        }

    }

    public List getDataScanCollectorList(){

        return dataScanCollectorList;
    }

    public Collection<SnapShotGenericValue> getValuesForVehicle(Vehicle v){

        //String, Map<Integer, SnapShotGenericValue>

        if(genericValuesMapList == null){
            return null;
        }

        Map<Integer, SnapShotGenericValue> map = genericValuesMapList.get(v.getVehicleId());

        if(map == null){
            return null;
        }

        return  map.values();

    }

    public Map<DCLifeSignParamEnum, SnapShotGenericValue> getPositionForVehicle(Vehicle v){

        Map<DCLifeSignParamEnum, SnapShotGenericValue> myMap = new HashMap<>();
        Map<Integer, SnapShotGenericValue> mapValues = genericValuesMapList.get(v.getVehicleId());
        if(mapValues == null){
            return myMap;
        }

        for(DCLifeSignParamEnum p : positionTagIdMap.keySet()){
            Integer tagId = positionTagIdMap.get(p);
            SnapShotGenericValue snv = mapValues.get(tagId);

            if(snv == null){
                continue;
            }
            myMap.put(p, snv);
        }


        return  myMap;

    }


    private void sendRefreshHistoryDataEvent(final Vehicle vehicle, EventTypeEnum e) {

        /*LOG.debug(String.format("HDRefresh %s:%s", vehicle.getVehicleId(),
                e.toString()));*/
        fireEvent(new DiagnosticsEvent(this), vehicle, e);
    }

    private void fireEvent(final DiagnosticsEvent eo, final Vehicle vehicle, final EventTypeEnum eventType) {
        for (final IDiagnosticsEventHandler evt : listeners) {
            Runnable runnable = new Runnable() {
                public void run() {
                    eo.setTagType(eventType);
                    eo.setCurrentVehicle(vehicle);
                    evt.handleDiagnosticsEvent(eo);

                }

            };
            new Thread(runnable).start();
        }
    }

    public void removeListener(IDiagnosticsEventHandler handler){
        this.listeners.remove(handler);
        LOG.debug(String.format("remove Listener %s", this.listeners.size()));
    }

    public void addListener(IDiagnosticsEventHandler handler){
        this.listeners.add(handler);
        LOG.debug(String.format("add Listener %s", this.listeners.size()));
    }

    public void init() {
        load();
        updatePositionTagMap();
    }

    public Map<DCLifeSignParamEnum, Integer> getPositionTagIdMap() {
        return positionTagIdMap;
    }
}
