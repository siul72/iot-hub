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


package co.luism.lukisoftiot.webapputils;


import co.luism.ksoft.iot.utils.common.ProcessDataTag;
import co.luism.ksoft.iot.utils.configuration.BaseConfigurationManager;
import co.luism.ksoft.iot.utils.configuration.UtilsConfiguration;
import co.luism.ksoft.iot.utils.enterprise.*;
import co.luism.lukisoftiot.common.*;
import co.luism.lukisoftiot.datacollector.DataCollectorDataScanner;
import co.luism.lukisoftiot.datacollector.DataCollectorDiagnosticEventPuller;
import co.luism.lukisoftiot.datacollector.DataCollectorSocket;
import co.luism.lukisoftiot.datacollector.messages.DCLifeSign;
import co.luism.lukisoftiot.enterprise.*;
import co.luism.lukisoftiot.interfaces.IDiagnosticsEventHandler;
import co.luism.lukisoftiot.utils.HibernateUtil;
import co.luism.lukisoftiot.utils.TagMapKey;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * WebManager is the class for initializer all OnlineDataCollector process
 *
 * @author      Luis Coelho
 * @version     %I%, %G%
 * @since       1.00.00
 */
public final class WebAppUtils {
	
	private DataCollectorSocket myDCSocket = null;
    private DataCollectorDiagnosticEventPuller myDCAlarmPoll = null;
    private Set<TagMapKey> tagMapKeys = new HashSet<>();
    private Map<TagMapKey, DataTag> myEventDataTagMap = new TreeMap<>();
    private Map<TagMapKey, DataTag> myProcessDataTagMap = new TreeMap<>();
    private Map<TagMapKey,DataTag> mySystemDataTagMap = new TreeMap<>();
    private final Map<String, Organization> organizationMap = new HashMap<>();
    private final Set<AlarmBuffer> alarmBufferSet = new HashSet<>();
    private final Set<AlarmCategory> categorySet = new TreeSet<>();
    private static final Logger LOG = Logger.getLogger(WebAppUtils.class);
	private static WebAppUtils instance = null;
    private final Set<IDiagnosticsEventHandler> listeners = new HashSet<>();

	private boolean isStarted = false;
    private VehicleDataSynchronization myAlarmLoader;

    ExecutorService backOldEventsExecutor = Executors.newFixedThreadPool(DiagnosticsConfig.MAX_BACK_THREADS);

    public static WebAppUtils getInstance() {
		if(instance == null){
			instance = new WebAppUtils();
		}
		return instance;
	}
	
	public <T> void init(Class clazz){
    	if(isStarted ){
			LOG.info("already started");
			return;
		}
        isStarted = true;
        //to init it
        LanguageManager.getInstance();
        DiagnosticsConfig myConfig = DiagnosticsConfig.setInstance(clazz);
        for(Organization myOrg : loadListFromDatabase(Organization.class)){
            this.organizationMap.put(myOrg.getName(), myOrg);
        }

        updateVehiclesConfigurationId(this.organizationMap);

        this.alarmBufferSet.addAll(loadListFromDatabase(AlarmBuffer.class));
        this.categorySet.addAll(loadListFromDatabase(AlarmCategory.class));
        List<DataTag> myList = loadListFromDatabase(DataTag.class);

        for (DataTag myTag : myList){
            putOnMap(myTag);
        }

		DataCollectorSocket.setInstance(myConfig.getServerPort());
		myDCSocket = DataCollectorSocket.getInstance();
		myDCSocket.init();
        myAlarmLoader = VehicleDataSynchronization.getInstance();
        myAlarmLoader.init();

        List<Vehicle> myVehicleList = WebManagerFacade.getInstance().getAllVehicles();
        for (Vehicle vehicle : myVehicleList){
            vehicle.addListener(myAlarmLoader);
        }
        DCLifeSign.getInstance();
        //start alarm polling data
        myDCAlarmPoll = DataCollectorDiagnosticEventPuller.getInstance();
        myDCAlarmPoll.init();
    }

    private void updateVehiclesConfigurationId(Map<String, Organization> organizationMap) {
        for(Organization org : organizationMap.values()){
            for(Configuration cnf : org.getConfigurationMap().values()){
                for(Fleet fl : cnf.getFleetMap().values()){
                    for(Vehicle v : fl.getVehicleMap().values()){
                        v.setConfigurationId(fl.getConfigurationId());
                    }
                }
            }
        }
    }

    private void putOnMap(DataTag myTag) {

        EventTypeEnum t = EventTypeEnum.getEnum(myTag.getType());
        if(t == null){
            LOG.error("invalid type");
            return;
        }

        Map<TagMapKey, DataTag> myMap;
        Integer cnfId, id;
        switch (t){
            case TAG_DATA_TYPE_PD:
                myMap = this.myProcessDataTagMap;
                cnfId = myTag.getConfigurationId();
                id = myTag.getSourceTagId();
            break;
            case TAG_DATA_TYPE_EVENT:
                myMap = this.myEventDataTagMap;
                cnfId = myTag.getConfigurationId();
                id = myTag.getSourceTagId();
                break;
            case TAG_DATA_TYPE_SYSTEM:
                myMap = this.mySystemDataTagMap;
                cnfId = DiagnosticsConfig.TAG_SYSTEM_DEFAULT_CONFIG_ID;
                id = myTag.getTagId();
                break;
            default:
                return;
        }

        TagMapKey k = findTagMapKey( cnfId, id);
        if(k == null){
            k = new TagMapKey( cnfId, id);
            this.tagMapKeys.add(k);
        }

        myMap.put(k, myTag);


    }

    private TagMapKey findTagMapKey(Integer configurationId, Integer id) {

        for(TagMapKey tagMapKey : this.tagMapKeys){
            if(tagMapKey.sourceTagId == id && tagMapKey.configurationId== configurationId){
                return tagMapKey;
            }
        }

        return null;
    }

    private DataTag readFromMap( EventTypeEnum t, int configurationId, int sourceTagId) {


        Map<TagMapKey, DataTag> myMap;

        switch (t){
            case TAG_DATA_TYPE_PD:
                myMap = this.myProcessDataTagMap;
                break;
            case TAG_DATA_TYPE_EVENT:
                myMap = this.myEventDataTagMap;
                break;
            default:
                return null;
        }

        TagMapKey k = findTagMapKey(configurationId, sourceTagId);

        if(k == null){
            return null;
        }

        return myMap.get(k);

    }

    private DataTag readFromMap(EventTypeEnum tagDataType, int configurationId, String name) {
        Map<TagMapKey, DataTag> myMap;

        switch (tagDataType){
            case TAG_DATA_TYPE_PD:
                myMap = this.myProcessDataTagMap;
                break;
            case TAG_DATA_TYPE_EVENT:
                myMap = this.myEventDataTagMap;
                break;
            case TAG_DATA_TYPE_SYSTEM:
                myMap = this.mySystemDataTagMap;
            break;
            default:
                return null;
        }

        if(tagDataType.equals(EventTypeEnum.TAG_DATA_TYPE_PD) ||
                tagDataType.equals(EventTypeEnum.TAG_DATA_TYPE_EVENT)){

            for (DataTag tag : myMap.values()){
                if(tag.getConfigurationId() == configurationId){
                    if(tag.getName().equals(name)){
                        return tag;
                    }
                }
            }

        } else {

            for (DataTag tag : myMap.values()){
                if(tag.getName().equals(name)){
                      return tag;
                    }
            }

        }

        return null;

    }

    public Set<AlarmBuffer> getBufferSet() {
        return alarmBufferSet;
    }

    public Set<AlarmCategory> getCategorySet() {
        return categorySet;
    }

    public <T> List<T> loadListFromDatabase(Class<T> clazz) {

        List <T> loadToList = HibernateUtil.getInstance().getList(clazz);

        LOG.debug("**************");
        LOG.debug(String.format("Got %d elements for %s", loadToList.size(), clazz.getSimpleName()));
        LOG.debug("**************");
        return loadToList;

    }

	public void close() {
        isStarted = false;
        myAlarmLoader.close();
        myDCAlarmPoll.close();
		myDCSocket.close();
		
	}

    Fleet findFleet(String fleetName) {

        for( Organization org : organizationMap.values()){

            for(Configuration cnf : org.getConfigurationMap().values()) {
                for (Fleet f : cnf.getFleetMap().values()) {
                     if(f.getName().equals(fleetName)){
                         return f;
                     }
                }
            }
        }

        return null;
    }

	Vehicle findVehicle(String vID) {

        for( Organization org : organizationMap.values()){

            for(Configuration cnf : org.getConfigurationMap().values()) {
                for (Fleet f : cnf.getFleetMap().values()) {
                    Vehicle v = f.getVehicleMap().get(vID);
                    if (v != null) {
                        return v;
                    }
                }
            }
        }

		return null;
	}


    void addListener(IDiagnosticsEventHandler listener) {
        LOG.debug("new listener");
        listeners.add(listener);
    }

    void removeListener(IDiagnosticsEventHandler listener) {

       if(listeners.contains(listener)){
           listeners.remove(listener);
       }
       LOG.debug("remove listener");
    }

    private void fireEvent(final DiagnosticsEvent eo, final Vehicle v, final int ts, final EventTypeEnum type, final List<Integer> listOfItems) {

        LOG.debug(String.format("Event: type:%s vehicle:%s listeners:%d", type.toString() ,v.getVehicleId(), listeners.size()));

        for (final IDiagnosticsEventHandler evt : listeners) {
            Runnable runnable = new Runnable() {
                public void run() {
                    eo.setTimeStamp(ts);
                    eo.setTagType(type);
                    eo.setListOfUpdatedItems(listOfItems);
                    eo.setCurrentVehicle(v);
                    evt.handleDiagnosticsEvent(eo);

                }

            };
            new Thread(runnable).start();
        }
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

    public void sendDataUpdateEvent(final Vehicle v, EventTypeEnum typeEnum, int ts, final List<Integer> listOfItems) {

        if(listOfItems.isEmpty()){
            this.LOG.error("The size of process data updated items is invalid");
            return;
        }

        fireEvent(new DiagnosticsEvent(this), v, ts, typeEnum, listOfItems);
    }

    public void sendStatusUpdateEvent(final Vehicle vehicle) {

        LOG.debug(String.format("new status for vehicle %s -> %s", vehicle.getVehicleId(),
                vehicle.getStatus().toString()));
        fireEvent(new DiagnosticsEvent(this), vehicle, EventTypeEnum.TAG_DATA_TYPE_SYSTEM);
    }

    public void sendRefreshAlarmsEvent(final Vehicle vehicle) {

        LOG.debug(String.format("Alarm Refresh for vehicle %s", vehicle.getVehicleId()));
        fireEvent(new DiagnosticsEvent(this), vehicle, EventTypeEnum.ALARM_REFRESH);
    }


    public DataTag findTagBySourceTagId(EventTypeEnum type,int configurationId, int sourceId) {

         return readFromMap(type, configurationId, sourceId);

    }

    public Map<String, Organization> getOrganizationMap() {
        return organizationMap;
    }

    public boolean add(Organization bean) {

        this.organizationMap.values().add(bean);
        return true;
    }

    public boolean add(User bean) {

        for (Organization org: this.organizationMap.values()){


            if(bean.getOrganizationId().equals(org.getOrganizationId())){
                //org.getUserList().add(bean);
                org.getUserMap().put(bean.getLogin(), bean);
                return true;
            }
        }

        return false;
    }

    public boolean add(Configuration bean) {

        for (Organization org: this.organizationMap.values()){
            if(bean.getOrganizationId().equals(org.getOrganizationId())){
                //org.getFleetList().add(bean);
                org.getConfigurationMap().put(bean.getProjectCode(), bean);
                return true;
            }
        }

        return false;

    }

    public boolean add(Fleet bean) {

        for (Organization org: this.organizationMap.values()){
            for(Configuration cnf : org.getConfigurationMap().values()){
                if(bean.getConfigurationId().equals(cnf.getConfigurationId())){
                    cnf.getFleetMap().put(bean.getName(), bean);
                    return true;
                }
            }
        }

        return false;

    }

    public boolean add(Vehicle bean) {

        for (Organization org: this.organizationMap.values()){
            for(Configuration cnf : org.getConfigurationMap().values()){
                for(Fleet f : cnf.getFleetMap().values()){
                    if(bean.getFleetId().equals(f.getFleetId())){
                        f.getVehicleMap().put(bean.getVehicleId(), bean);
                        return true;
                    }
                }
            }


        }

        return false;

    }

    public boolean add(DataTag bean) {

       putOnMap(bean);
       return true;

    }


    public void loadEventConfigurationToDatabase(BaseConfigurationManager cnf) {

       //cnf.getProjekt()

        Configuration myConf = saveOrganization(cnf);

        if(myConf == null){
            LOG.error("unable to create or update configuration");
        }

        saveVehicles(myConf, cnf);
        saveDataTags(myConf, cnf);
        saveCategories(myConf, cnf);
    }

    public void saveCategories(Configuration myConf, BaseConfigurationManager cnf) {

        Map<String, Object> myRestrictions = new HashMap<>();
        int configurationId = myConf.getConfigurationId();
        //first create or update buffers
        for(Category c : cnf.getCategories()){

            if(c.getId() == null){
                LOG.error(String.format("Category as no ID %s", c.getName()));
                continue;
            }
            myRestrictions.clear();
            myRestrictions.put("configurationId", configurationId);
            myRestrictions.put("bufferIndex", c.getBuffer().getBufferID());
            //find buffer
            AlarmBuffer alarmBuffer = AlarmBuffer.read(AlarmBuffer.class, myRestrictions);
            if(alarmBuffer == null){
               alarmBuffer = new AlarmBuffer(c.getBuffer(), configurationId);
               alarmBuffer.create();

            }

            //find buffer
            alarmBuffer = AlarmBuffer.read(AlarmBuffer.class, myRestrictions);

            myRestrictions.clear();
            myRestrictions.put("bufferId", alarmBuffer.getBufferId());
            myRestrictions.put("name", c.getName());

            AlarmCategory alarmCategory = this.findCategory(myRestrictions);

            if(alarmCategory == null) {
                alarmCategory = AlarmCategory.read(AlarmCategory.class, myRestrictions);
            }

            if(alarmCategory == null) {
                alarmCategory = new AlarmCategory(c.getId(), c.getName(), alarmBuffer.getBufferId());
                alarmCategory.setMyBuffer(alarmBuffer);
                alarmCategory.create();
            } else {
                alarmCategory.setCategoryIndex(c.getId());
                alarmCategory.setName(c.getName());
                alarmCategory.setBufferId(alarmBuffer.getBufferId());
                alarmCategory.setMyBuffer(alarmBuffer);
                alarmCategory.update();
            }

            //read back
            alarmCategory = AlarmCategory.read(AlarmCategory.class, myRestrictions);

            if(alarmCategory == null){
                LOG.error(String.format("category not found for %s and %s",
                        myRestrictions.get("projectCode"),
                        myRestrictions.get("name")));
                continue;
            }

            LOG.debug("about to delete category signal mapping");
            //delete previous mapped values for the category
            for(CategorySignalMap cs : alarmCategory.getWebMapCategorySignalMap().values()){
                cs.delete();
            }

            alarmCategory.getWebMapCategorySignalMap().clear();

            //read back again
            alarmCategory = AlarmCategory.read(AlarmCategory.class, myRestrictions);

            Integer position = 0;

            for(DataTagValue t : c.getMyDigitalSignals()){

                CategorySignalMap sigMap = new CategorySignalMap(alarmCategory.getCategoryId(),
                        t.getId(), t.getType().getSize(), position++);
                alarmCategory.getWebMapCategorySignalMap().put(t.getId(), sigMap);
            }

            for(DataTagValue t : c.getMyAnalogSignals()){

                CategorySignalMap sigMap = new CategorySignalMap(alarmCategory.getCategoryId(),
                        t.getId(), t.getType().getSize(), position++);
                alarmCategory.getWebMapCategorySignalMap().put(t.getId(), sigMap);
            }

            alarmCategory.update();


        }

        this.categorySet.clear();
        this.categorySet.addAll(loadListFromDatabase(AlarmCategory.class));
    }

    private AlarmCategory findCategory(Map<String, Object> myRestrictions) {
        for(AlarmCategory cat : this.categorySet){

            if(cat.getBufferId().equals(myRestrictions.get("bufferId"))){
                if(cat.getName().equals(myRestrictions.get("name"))){
                    return cat;
                }
            }

        }

        return null;
    }

    public AlarmCategory getCategory(Integer categoryIndex) {
        for(AlarmCategory cat : this.categorySet){
            if(cat.getCategoryIndex().equals(categoryIndex)){
                return cat;
            }


        }

        return null;
    }


    private void saveDataTags(Configuration myConf, BaseConfigurationManager cnf) {
        LOG.debug("start save/update event data tags...");
        boolean isNew;

        Map<Integer, String> myMap = new HashMap<>();

        myMap.put(0, "de");
        myMap.put(1, "fr");
        myMap.put(2, "it");
        myMap.put(3, "en");

        for(DataTagEke ekeTag : cnf.getDataTagContainer().getEKEVector()){

            if(!ekeTag.isEreignis()){
                continue;
            }

            DataTagEvent dataTagEvent = ekeTag.getEvent();
            int sourceTagId = dataTagEvent.getId();
            int configurationId = myConf.getConfigurationId();

            Map<String, Object> myRestrictions = new HashMap<>();
            myRestrictions.put("configurationId", configurationId);
            myRestrictions.put("sourceTagId", sourceTagId);
            myRestrictions.put("type", EventTypeEnum.TAG_DATA_TYPE_EVENT.getValue());

            DataTag tag = this.getTagBySourceTagId(EventTypeEnum.TAG_DATA_TYPE_EVENT, configurationId, sourceTagId);
            isNew = false;
            if(tag == null){

                tag = DataTag.read(DataTag.class, myRestrictions);
                if(tag == null){
                    tag = new DataTag(EventTypeEnum.TAG_DATA_TYPE_EVENT, configurationId, sourceTagId, dataTagEvent.getName());
                    isNew = true;
                }
            }

            tag.setName(ekeTag.getName());
            tag.setEnabled(dataTagEvent.isReact());
            //tag.setType(EventTypeEnum.TAG_DATA_TYPE_EVENT.getValue());
            tag.setValueType(ekeTag.getType().getValue());
            tag.setEngUnits(ekeTag.getUnit().getValue());
            tag.setPreData(dataTagEvent.isPreHistory());
            tag.setPostData(dataTagEvent.isPostHistory());

            if(isNew) {
                if (!tag.create()) {
                    LOG.error(String.format("Unable to create tag %d", sourceTagId));
                    //readback the vakue
                    tag = DataTag.read(DataTag.class, myRestrictions);
                    continue;
                } else {
                    LOG.debug(String.format("Tag for %s created", tag.getName()));
                }
            } else {
                if (!tag.update()) {
                    LOG.error(String.format("Unable to update tag %d", sourceTagId));
                    continue;
                } else {
                    LOG.debug(String.format("Tag for %s updated", tag.getName()));
                }

            }
            //dataTagEvent.getKurztext()
            LOG.debug("start save AlarmTagDescription...");
            for(int i=0 ; i < UtilsConfiguration.MAX_LANGUAGES ; i++){
                AlarmTagDescription alarmTagDescription = AlarmTagDescription.getInstance(tag.getTagId(), myMap.get(i));
                alarmTagDescription.setLanguage(myMap.get(i));
                alarmTagDescription.setTagId(tag.getTagId());
                alarmTagDescription.setShortDescription(dataTagEvent.getKurztext()[i]);
                alarmTagDescription.setLongDescription(dataTagEvent.getText()[i][0]);
                alarmTagDescription.setWorkshopDescription(dataTagEvent.getText()[i][2]);
                alarmTagDescription.update();

                LanguageManager.getInstance().addDescription(alarmTagDescription);
            }
            LOG.debug("... finish start save AlarmTagDescription...");
        }


        LOG.debug("... finish save/update event data tags");
    }


    private void saveVehicles(Configuration myConfiguration, BaseConfigurationManager cnf) {

        LOG.debug("start saving/updating vehicles...");
        //we have to create a dummy fleet for this vehicles
        String fleetName = DiagnosticsConfig.DEFAULT_FLEET_NAME;

        Fleet f = myConfiguration.getFleetMap().get(fleetName);
        boolean isNew = false;
        if(f == null){
            f = Fleet.read(Fleet.class, "name", fleetName);
            if(f == null){
                f = new Fleet(fleetName);
                isNew = true;
            }

            f.setMyConfiguration(myConfiguration);
            f.setConfigurationId(myConfiguration.getConfigurationId());
            f.getVehicleMap().clear();

            if(isNew) {
                if (!f.create()) {
                    LOG.error("Unable to create default fleet");
                    return;
                }
            } else {
                if (!f.update()) {
                    LOG.error("Unable to update default fleet");
                    return;
                }

            }

            //now read it back from database
            f = Fleet.read(Fleet.class, "name", fleetName);
        }


        for (Fahrzeug fahrzeug : cnf.getFahrzeugList())
        {
            String vId = fahrzeug.getMD5Hash();
            //first try to get vehicle from database
            isNew = false;
            Vehicle v = this.findVehicle(vId);
            //if not found create
            if(v == null){
                v = Vehicle.read(Vehicle.class, "vehicleId", vId);
                if(v == null){
                    v = new Vehicle(vId);
                    isNew = true;
                }

            }

            v.setVehicleType(fahrzeug.getFahrzeugName());
            v.setVehicleNumber(fahrzeug.getEinbauort());
            v.setMyFleet(f);
            v.setFleetId(f.getFleetId());
            if(isNew) {
                if (!v.create()) {
                    LOG.error("Unable to create vehicle");
                    continue;
                }
            } else {
                if (!v.update()) {
                    LOG.error("Unable to update vehicle");
                    continue;
                }

            }

        }

        LOG.debug("... finish  saving/updating vehicles");

    }
    private Configuration saveOrganization(BaseConfigurationManager cnf) {

        String orgName = cnf.getProjekt();
        String projectCode = cnf.getProjekthash();
        Organization myOrg = this.organizationMap.get(orgName);
        String email = String.format("admin@%s.com",cnf.getProjekt().toLowerCase().replaceAll("\\s",""));

        if(myOrg == null){

            myOrg = Organization.read(Organization.class, "name", orgName);
            if( myOrg == null){
                myOrg = new Organization(orgName, email);
                myOrg.create();
                myOrg = Organization.read(Organization.class, "name", orgName);
            }


        }
        Configuration configuration = myOrg.getConfigurationMap().get(projectCode);
        //find Configuration
        if(configuration == null){
            configuration = Configuration.read(Configuration.class, "projectCode", projectCode);
            if(configuration == null) {
                configuration = new Configuration();
                configuration.create();
                LOG.debug("new configuration created");
            } else {

                Organization other =  configuration.getMyOrganization();
                other.getConfigurationMap().remove(configuration.getProjectCode());
            }

            configuration.setMyOrganization(myOrg);
            configuration.setOrganizationId(myOrg.getOrganizationId());
            configuration.setHardware(cnf.getHardware().getValue());
            configuration.setProjectCode(projectCode);
            configuration.setVersion(cnf.getVersion());
            configuration.update();
            myOrg.getConfigurationMap().put(projectCode, configuration);

        }

        configuration = Configuration.read(Configuration.class, "projectCode", projectCode);
        return configuration;
    }

    public Configuration getConfiguration(BaseConfigurationManager cnf) {

        String orgName = cnf.getProjekt();
        String projectCode = cnf.getProjekthash();
        Organization myOrg = this.organizationMap.get(orgName);

        if(myOrg == null){

            return null;

        }

        return myOrg.getConfigurationMap().get(projectCode);
    }

    public void loadProcessDataConfigurationToDatabase(Set<ProcessDataTag> mySet, Integer configurationId) {

        Boolean isNew;

        for(ProcessDataTag pdTag : mySet){

            int sourceTagId = pdTag.getId();

            DataTag tag = this.getTagBySourceTagId(EventTypeEnum.TAG_DATA_TYPE_PD, configurationId, sourceTagId);
            isNew = false;

            if(tag == null){
                Map<String, Object> myRestrictions = new HashMap<>();
                myRestrictions.put("configurationId", configurationId);
                myRestrictions.put("sourceTagId", sourceTagId);
                myRestrictions.put("type", EventTypeEnum.TAG_DATA_TYPE_PD.getValue());
                tag = DataTag.read(DataTag.class, myRestrictions);
                if(tag == null){
                    tag = new DataTag(EventTypeEnum.TAG_DATA_TYPE_PD, configurationId ,sourceTagId, pdTag.getName());
                    isNew = true;
                }

            } else {
                LOG.debug(String.format("got %s from memory", tag.getName()));
            }

            if(tag.getType() != EventTypeEnum.TAG_DATA_TYPE_PD.getValue()){
                LOG.error("something wrong with the tag type");
                continue;
            }

            //tag.setSourceTagId(sourceTagId);
            //tag.setType(EventTypeEnum.TAG_DATA_TYPE_PD.getValue());
            tag.setValueType(pdTag.getDataType());


            if(isNew) {
                if (!tag.create()) {
                    LOG.error(String.format("Unable to create tag %d", sourceTagId));
                    continue;
                } else {
                    LOG.debug(String.format("Tag for %s created", tag.getName()));
                }
            } else {
                if (!tag.update()) {
                    LOG.error(String.format("Unable to update tag %d", sourceTagId));
                    continue;
                } else {
                    LOG.debug(String.format("Tag for %s updated", tag.getName()));
                }

            }

        }

    }

    void reloadOrganization(){

        this.organizationMap.clear();

        for(Organization myOrg : loadListFromDatabase(Organization.class)){
            this.organizationMap.put(myOrg.getName(), myOrg);
        }
    }

    void restartWebManager(Class clazz){
        close();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            LOG.warn(e);

        }

        init(clazz);
    }

    void reloadDataTags() {

        this.myEventDataTagMap.clear();
        this.myProcessDataTagMap.clear();
        this.mySystemDataTagMap.clear();

        for (DataTag myTag : loadListFromDatabase(DataTag.class)){
            putOnMap(myTag);
        }


    }

    DataTag getTagBySourceTagId(EventTypeEnum tagDataTypeEvent, int configurationId, int sourceTagId) {

        return readFromMap(tagDataTypeEvent, configurationId, sourceTagId);
    }

    public DataTag getTagByName(EventTypeEnum eventTypeEnum, int configurationId, String name) {

        return readFromMap(eventTypeEnum, configurationId, name);
    }

    public DataTag getAlarmTagById(Integer id) {

        for(DataTag t : this.myEventDataTagMap.values()){
            if(t.getTagId().equals(id)){
                return t;
            }
        }

        return null;

    }



    List<DataTag> getAllTags() {
        List<DataTag> myList = new ArrayList<>();
        myList.addAll(this.myEventDataTagMap.values());
        myList.addAll(this.myProcessDataTagMap.values());
        myList.addAll(this.mySystemDataTagMap.values());
        return myList;
    }

    public DataTag getTagById(Integer id) {

        for(DataTag t : this.myEventDataTagMap.values()){
            if(t.getTagId().equals(id)){
                return t;
            }
        }

        for(DataTag t : this.myProcessDataTagMap.values()){
            if(t.getTagId().equals(id)){
                return t;
            }
        }

        for(DataTag t : this.mySystemDataTagMap.values()){
            if(t.getTagId().equals(id)){
                return t;
            }
        }

        return null;
    }

    User getUser(String login) {
        for(Organization org : this.organizationMap.values()){
             User u = org.getUserMap().get(login);
            if(u != null){
                return u;
            }
        }

        return null;
    }

    public List<AlarmCategory> getCategoriesForEvent(AlarmValueHistoryInfo alarmValueHistoryInfo) {
        List<AlarmCategory> myList = new ArrayList<>();

        for(AlarmCategory cat : this.categorySet){

            if(alarmValueHistoryInfo.getMyTag() == null){
                continue;
            }

            CategorySignalMap sigMap = cat.getWebMapCategorySignalMap().get(alarmValueHistoryInfo.getMyTag().getSourceTagId());
            if(sigMap != null){
                 myList.add(cat);
                }
            }
        return myList;

    }

    public void deleteDataTags() {

        this.mySystemDataTagMap.clear();
        this.myProcessDataTagMap.clear();
        this.myEventDataTagMap.clear();

        Utils.tableDeleteContents(HistoryAlarmTagValue.class);
        Utils.tableDeleteContents(SnapShotAlarmTagValue.class);
        Utils.tableDeleteContents(DataTag.class);
    }


    public void reloadDataScanner() {
        DataCollectorDataScanner.getInstance().reload();

    }


    public AlarmValueHistoryInfo getAlarmValueByIndex(int eventIndex, String vehicleId) {
        Map<String, Object> myRestrictions = new HashMap<>();
        myRestrictions.put("eventIndex", eventIndex);
        myRestrictions.put("vehicleId", vehicleId);

        return AlarmValueHistoryInfo.read(AlarmValueHistoryInfo.class, myRestrictions);
    }

    public Collection<Vehicle> getOfflineVehicleCollection() {
        List<Vehicle> myList = WebManagerFacade.getInstance().getAllVehicles();
        List<Vehicle> myCollection = new ArrayList<>();

        for(Vehicle v : myList){
            if(v.getStatus().equals(VehicleStatusEnum.ST_ONLINE) ||
                    v.getStatus().equals(VehicleStatusEnum.ST_SUSPENDED)){
                continue;
            }
            myCollection.add(v);
        }

        return myCollection;

    }

    public boolean isStarted() {
        return isStarted;
    }

    public void backupOldEventData(Vehicle v) {
        ProcessBackupEvents myProcess = new ProcessBackupEvents(v);
        backOldEventsExecutor.execute(myProcess);

    }

    public DataTag getTagByProcessId(Integer processId) {

        for(DataTag t : myProcessDataTagMap.values()){
            if(t.getProcess().equals(processId)){
                return t;
            }
        }

        return null;
    }
}
