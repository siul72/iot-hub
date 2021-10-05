package co.luism.lukisoftiot.webapputils;

import co.luism.ksoft.iot.utils.common.OpenOfficeDataSheetManager;
import co.luism.ksoft.iot.utils.common.ProcessDataTag;
import co.luism.ksoft.iot.utils.configuration.BaseConfigurationManager;
import co.luism.ksoft.iot.utils.configuration.ConfigurationManagerUtils;
import co.luism.lukisoftiot.common.XmlList;

import co.luism.lukisoftiot.datacollector.*;
import co.luism.lukisoftiot.datacollector.common.DataCollectorFrameSender;
import co.luism.lukisoftiot.datacollector.common.DCDPullerEventHandler;
import co.luism.lukisoftiot.datacollector.common.DCParserAckNackEventHandler;
import co.luism.lukisoftiot.datacollector.common.DataCollectorImportUtils;
import co.luism.lukisoftiot.common.DiagnosticsPersistent;
import co.luism.lukisoftiot.common.EventTypeEnum;
import co.luism.lukisoftiot.common.ReturnCode;

import co.luism.lukisoftiot.enterprise.*;
import co.luism.lukisoftiot.interfaces.IDiagnosticsEventHandler;
import co.luism.lukisoftiot.interfaces.IWebManagerFacade;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by luis on 05.09.14.
 */
public class WebManagerFacade implements IWebManagerFacade {

    private static final Logger LOG = Logger.getLogger(WebManagerFacade.class);
    private static WebManagerFacade instance = null;

    public static WebManagerFacade getInstance(){
        if (instance == null){
            instance = new WebManagerFacade();
        }

        return instance;
    }


    @Override
    public ReturnCode ack() {
        return null;
    }

    @Override
    public ReturnCode nack() {
        return null;
    }

    @Override
    public ReturnCode validateUser(String login, String password) {

        //User u = User.read(User.class, "login", login);

        User u = WebAppUtils.getInstance().getUser(login);

        if(u == null){
            return ReturnCode.RET_NOK;
        }

        try {
            return (u.authenticate(password))  ? ReturnCode.RET_OK : ReturnCode.RET_NOK;
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e);
        }

        return ReturnCode.RET_NOK;
    }

    @Override
    public List<Organization> getOrganizationList() {
        List<Organization> myOrgList = new ArrayList<>();
        myOrgList.addAll(WebAppUtils.getInstance().getOrganizationMap().values());
        return myOrgList;
    }

    @Override
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        for(Organization o : this.getOrganizationList()){
            users.addAll(o.getUserMap().values());
        }

        return users;
    }

    @Override
    public List<Configuration> getAllConfigurations() {

        List<Configuration> configurations = new ArrayList<>();

        for(Organization o : this.getOrganizationList()){
            configurations.addAll(o.getConfigurationMap().values());
        }

        return configurations;
    }

    @Override
    public List<DataScanCollector> getAllDataScanCollectors() {


        return DataCollectorDataScanner.getInstance().getDataScanCollectorList();


    }

    @Override
    public List<Fleet> getAllFleets() {
        List<Fleet> fleets = new ArrayList<>();

        for(Organization o : this.getOrganizationList()){
            for(Configuration cnf : o.getConfigurationMap().values()){
                fleets.addAll(cnf.getFleetMap().values());
            }

        }

        return fleets;
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        List<Fleet> fleets = getAllFleets();
        if(fleets != null){
            for(Fleet o : fleets){
                vehicles.addAll(o.getVehicleMap().values());
            }
        } else {
            LOG.info("fleet list is empty");
        }

        return vehicles;

    }

    @Override
    public List<Vehicle> getAllVehicles(User u) {
        List<Vehicle> myList = new ArrayList<>();
        if(u == null){
            return myList;
        }
        if(u.getMyOrganization() == null){
            return myList;
        }
        if(u.getMyOrganization().getConfigurationMap() == null){
            return myList;
        }
        for(Configuration cnf : u.getMyOrganization().getConfigurationMap().values()){
            if(cnf.getFleetMap() == null){
                continue;
            }
            for(Fleet f : cnf.getFleetMap().values()){
                if(f.getVehicleMap() == null){
                    continue;
                }
                myList.addAll(f.getVehicleMap().values());
            }
        }
        return myList;
    }

    @Override
    public List<DataTag> getAllTags() {

        return WebAppUtils.getInstance().getAllTags();
    }

    public User getUser(String loginName){

        User usr = null;
        for(Organization  org: this.getOrganizationList()) {
            usr = org.getUserMap().get(loginName);
            if(usr != null){
                return usr;
            }
        }

        return usr;

    }

    @Override
    public List<String> getAllVehicleTypes() {
        List<String> types = new ArrayList<>();

        for(Vehicle v : getAllVehicles()){
            String t = v.getVehicleType();
            if(t != null){
                types.add(t);
            }

        }

        return types;
    }

    @Override
    public List<Role> getAllRoles() {
        return Role.getList(Role.class);
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Permission.getList(Permission.class);
    }

    @Override
    public List<HistoryAlarmTagValue> getLastVehicleHistoryAlarmTagValues(String vehicleId, Integer numberOfValues) {

        Map<String, Object> myRestrictions = new HashMap<>();
        myRestrictions.put("vehicleId", vehicleId);

        return TagValue.getList(HistoryAlarmTagValue.class,myRestrictions, numberOfValues, "timeStamp");
    }

    @Override
    public List<AlarmValueHistoryInfo> getAlarmTagHistoryInfo(String vehicleId, Integer numberOfValues){

        Map<String, Object> myRestrictions = new HashMap<>();
        myRestrictions.put("vehicleId", vehicleId);
        myRestrictions.put("ack", false);
        return AlarmValueHistoryInfo.getList(AlarmValueHistoryInfo.class, myRestrictions, numberOfValues, "endTimeStamp");

    }

    @Override
    public List<AlarmEnvironmentData> getAlarmEnvironmentData(String vehicleId, Integer numberOfValues) {
        Map<String, Object> myRestrictions = new HashMap<>();
        myRestrictions.put("vehicleId", vehicleId);
        List<AlarmValueHistoryInfo> myHistoryAlarmList = AlarmValueHistoryInfo.getList(AlarmValueHistoryInfo.class, myRestrictions, numberOfValues, "endTimeStamp");
        List<AlarmEnvironmentData> myList = new ArrayList<>();
        for(AlarmValueHistoryInfo alarmValueHistoryInfo : myHistoryAlarmList){

            //get all
            myRestrictions.clear();
            myRestrictions.put("alarmTagHistoryInfoId", alarmValueHistoryInfo.getId());
            List elementList = AlarmEnvironmentData.getList(AlarmEnvironmentData.class, myRestrictions);
            myList.addAll(elementList);
            if(myList.size() > numberOfValues){
                break;
            }
        }

        return myList;

    }

    @Override
    public List<AlarmEnvironmentDataRequester> getAlarmEnvironmentDataRequesterList(Integer alarmInfoId) {
        return DataCollectorDiagnosticEventPuller.getInstance().getRequesterList(alarmInfoId);
    }

    @Override
    public boolean createData(DiagnosticsPersistent bean) {
         boolean status;
        if(bean.create()){
            if (bean instanceof Organization){
                WebAppUtils.getInstance().add((Organization) bean);
            }

            if (bean instanceof User){

                User u = (User) bean;

                if(u.getMyRole() == null){

                    u.setMyRole((Role) Role.read(Role.class, u.getRoleId()));
                }

                WebAppUtils.getInstance().add((User) bean);
            }

            if (bean instanceof Fleet){
                WebAppUtils.getInstance().add((Fleet) bean);
            }

            if (bean instanceof DataTag){
                WebAppUtils.getInstance().add((DataTag) bean);
            }

            if (bean instanceof Vehicle){
                WebAppUtils.getInstance().add((Vehicle) bean);
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean updateData(DiagnosticsPersistent bean, String updateBy) {

        bean.setUpdateBy(updateBy);
        return bean.update();
    }

    @Override
    public boolean reloadOrganization(){
        WebAppUtils.getInstance().reloadOrganization();
        return true;
    }

    @Override
    public boolean restartWebManager(Class clazz){
        WebAppUtils.getInstance().restartWebManager(clazz);
        return true;
    }

    @Override
    public boolean saveEventConfigurationToDataBase(File odsCognitioFileName) {
        BaseConfigurationManager cnf = new BaseConfigurationManager();

        if(!odsCognitioFileName.exists()){
            LOG.error("file no found " + odsCognitioFileName);
            return false;
        }

        //WebManager.getInstance().deleteDataTags();
        ConfigurationManagerUtils.loadProjectPropertiesFromODS(cnf, odsCognitioFileName);
        WebAppUtils.getInstance().loadEventConfigurationToDatabase(cnf);
        return true;
    }

    @Override
    public boolean saveProcessConfigurationToDataBase(File odsProcessFileName, Integer configurationId) {
        Set<ProcessDataTag> mySet = OpenOfficeDataSheetManager.getProcessDataTagSet(odsProcessFileName);
        if(mySet.isEmpty()){
            LOG.error("no process data found ");
            return false;
        }
        WebAppUtils.getInstance().loadProcessDataConfigurationToDatabase(mySet, configurationId);
        return true;
    }

    @Override
    public boolean saveCategoriesConfigurationToDataBase(File odsCognitioFileName) {
        BaseConfigurationManager cnf = new BaseConfigurationManager();

        if(!odsCognitioFileName.exists()){
            LOG.error("file no found " + odsCognitioFileName);
            return false;
        }

        ConfigurationManagerUtils.loadProjectPropertiesFromODS(cnf, odsCognitioFileName);

        Configuration myConf = WebAppUtils.getInstance().getConfiguration(cnf);

        WebAppUtils.getInstance().saveCategories(myConf, cnf);
        return true;
    }

    @Override
    public void reloadDataTags() {
        WebAppUtils.getInstance().reloadDataTags();
    }

    @Override
    public void reloadDataScanner() {
        WebAppUtils.getInstance().reloadDataScanner();
    }

    @Override
    public void addListener(IDiagnosticsEventHandler listener) {
        WebAppUtils.getInstance().addListener(listener);
    }

    @Override
    public void removeListener(IDiagnosticsEventHandler listener) {
        WebAppUtils.getInstance().removeListener(listener);
    }

    @Override
    public void removeVehicleAlarmHistoryListener(String vehicleId, DCDPullerEventHandler handler) {
        DataCollectorDiagnosticEventPuller.getInstance().removeListener(vehicleId, handler);
    }

    @Override
    public void replaceVehicleAlarmHistoryListener(String oldVehicleId, String newVehicleId, DCDPullerEventHandler handler) {
        DataCollectorDiagnosticEventPuller.getInstance().replaceListener(oldVehicleId, newVehicleId, handler);
    }


    @Override
    public DataTag getAlarmTagById(Integer id) {
        return WebAppUtils.getInstance().getAlarmTagById(id);
    }

    @Override
    public DataTag getTagBySourceId(EventTypeEnum t, int configurationId, int sourceId) {
        return WebAppUtils.getInstance().getTagBySourceTagId(t, configurationId, sourceId);
    }

    @Override
    public List<AlarmBuffer> getAllBuffers() {
        List<AlarmBuffer> myList = new ArrayList<>();
        myList.addAll(WebAppUtils.getInstance().getBufferSet());

        return myList;
    }

    @Override
    public List<AlarmCategory> getAllCategories() {
        List<AlarmCategory> myList = new ArrayList<>();
        myList.addAll(WebAppUtils.getInstance().getCategorySet());

        return myList;
    }

    @Override
    public List<CategorySignalMap> getAllMapCategoryEvents() {
        List<AlarmCategory> myCategoryList = getAllCategories();
        List<CategorySignalMap> myList = new ArrayList<>();

        for(AlarmCategory cat : myCategoryList){

            if(!cat.getWebMapCategorySignalMap().isEmpty()){
                myList.addAll(cat.getWebMapCategorySignalMap().values());
            }


        }

        return myList;
    }

    @Override
    public Vehicle getVehicle(String vid) {
        return WebAppUtils.getInstance().findVehicle(vid);
    }

    @Override
    public DataTag getTagByName(EventTypeEnum tagDataTypeSystem, int configurationId, String name) {
        return WebAppUtils.getInstance().getTagByName(tagDataTypeSystem, configurationId, name);
    }

    @Override
    public DataTag getTagById(Integer id) {
        return WebAppUtils.getInstance().getTagById(id);
    }

    @Override
    public void addTagDescription(AlarmTagDescription alarmTagDescription) {
        LanguageManager.getInstance().addDescription(alarmTagDescription);
    }

    @Override
    public AlarmTagDescription getTagAlarmDescription(String language, Integer tagId) {
        return LanguageManager.getInstance().getTagAlarmDescription(language, tagId);
    }

    @Override
    public boolean sendVncStopMessage(String vehicleId, Integer port) {
        String guId = DataCollectorSocket.getInstance().getGUID(vehicleId);
        if(guId == null){
            LOG.warn("guId not found for " + vehicleId);
            return false;
        }
        return DataCollectorFrameSender.getInstance().sendVncStopMessage(guId, port);
    }

    @Override
    public boolean sendVncStartMessage(String vehicleId, Integer port) {

        String guId = DataCollectorSocket.getInstance().getGUID(vehicleId);
        if(guId == null){
            LOG.warn("guId not found for " + vehicleId);
            return false;
        }

        return DataCollectorFrameSender.getInstance().sendStartVNCMessage(guId, port);
    }

    @Override
    public void addDCParserAckNackListener(DCParserAckNackEventHandler handler) {
        DataCollectorParser.getInstance().addListener(handler);
    }

    @Override
    public void removeDCParserAckNackListener(DCParserAckNackEventHandler handler) {
        DataCollectorParser.getInstance().removeListener(handler);

    }

    @Override
    public ReturnCode changeUserPassword(User currentUser, String password, String newPasswordStr, String newPasswordStr2) {

        //check if verify password match
        if(!newPasswordStr.equals(newPasswordStr2)){
            return ReturnCode.USER_PASS_CHANGE_VERIFY_NOT_MATCH;
        }

        //check if current password match current user password
        if(WebManagerFacade.getInstance().validateUser(currentUser.getLogin(), password) != ReturnCode.RET_OK){
            return ReturnCode.USER_PASS_CHANGE_CURRENT_PASS_NOT_MATCH;
        }

        return setPassword(currentUser, newPasswordStr);
    }

    @Override
    public ReturnCode setPassword(User myUser, String newPassword){

        myUser.setPassword(newPassword);

        return myUser.update() ? ReturnCode.RET_OK : ReturnCode.RET_NOK;
    }

    @Override
    public ReturnCode importAlarms(Vehicle vehicle, IDiagnosticsEventHandler listener, File zipFile){

        return DataCollectorImportUtils.importAlarms(vehicle, listener, zipFile);
    }

    @Override
    public AlarmCategory getCategory(Integer categoryId) {

        return WebAppUtils.getInstance().getCategory(categoryId);

    }

    @Override
    public DataTag getTagByProcessId(Integer processId){
        return WebAppUtils.getInstance().getTagByProcessId(processId);
    }

    @Override
    public <T> List<T>  loadListFromXml(Class<T> clazz) {

        List xmlList = null;
        try {
            xmlList = XmlList.fromXml(clazz);
        } catch (JAXBException e) {
            LOG.error("unable to create to xml " + clazz.getSimpleName() + ":" + e.getMessage());
        }

        return xmlList;

    }

    @Override
    public <T> void saveListToXml(Class clazz, List<T> list){
        XmlList<T> xmlList = new XmlList<T>(clazz, list);
        try {
            xmlList.toXml(clazz);
        } catch (JAXBException e) {
            LOG.error("unable to save to xml " + clazz.getSimpleName() + ":" + e.getMessage());
        }
    }
}
