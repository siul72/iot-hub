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

package co.luism.diagnostics.interfaces;

import co.luism.datacollector.common.DCDPullerEventHandler;
import co.luism.datacollector.common.DCParserAckNackEventHandler;
import co.luism.diagnostics.common.DiagnosticsPersistent;
import co.luism.diagnostics.common.EventTypeEnum;
import co.luism.diagnostics.common.ReturnCode;

import co.luism.datacollector.AlarmEnvironmentDataRequester;
import co.luism.diagnostics.enterprise.*;


import java.io.File;
import java.util.Collection;
import java.util.List;


/**
 * Created by luis on 05.09.14.
 */
public interface IWebManagerFacade {
    <T> void saveListToXml(Class clazz, List<T> list);

    ReturnCode ack();
    ReturnCode nack();
    ReturnCode validateUser(String login, String password);
    List<Organization> getOrganizationList();
    List<User> getAllUsers();
    List<Configuration> getAllConfigurations();
    List<DataScanCollector> getAllDataScanCollectors();
    List<Fleet> getAllFleets();
    List<Vehicle> getAllVehicles();
    List<Vehicle> getAllVehicles(User u);
    Collection<DataTag> getAllTags();
    User getUser(String loginName);
    List<String> getAllVehicleTypes();
    List<Role> getAllRoles();
    List<Permission> getAllPermissions();
    List<HistoryAlarmTagValue> getLastVehicleHistoryAlarmTagValues(String vehicleId, Integer numberOfValues);
    List<AlarmValueHistoryInfo> getAlarmTagHistoryInfo(String vehicleId, Integer numberOfValues);
    List<AlarmEnvironmentData> getAlarmEnvironmentData(String vehicleId, Integer numberOfValues);
    List<AlarmEnvironmentDataRequester> getAlarmEnvironmentDataRequesterList(Integer alarmInfoId);
    <T extends DiagnosticsPersistent> boolean createData(T bean);
    boolean updateData(DiagnosticsPersistent bean, String updateBy);

    boolean restartWebManager(Class clazz);

    boolean saveEventConfigurationToDataBase(File odsCognitioFileName);
    boolean saveProcessConfigurationToDataBase(File odsProcessFileName, Integer configurationId);
    boolean saveCategoriesConfigurationToDataBase(File odsCognitioFileName);
    boolean reloadOrganization();
    void reloadDataTags();
    void reloadDataScanner();
    void addListener(IDiagnosticsEventHandler listener);
    void removeListener(IDiagnosticsEventHandler listener);
    void removeVehicleAlarmHistoryListener(String vehicleId, DCDPullerEventHandler handler);
    void replaceVehicleAlarmHistoryListener(String oldVehicleId, String newVehicleId, DCDPullerEventHandler handler);
    DataTag getAlarmTagById(Integer id);
    DataTag getTagBySourceId(EventTypeEnum t, int configurationId, int sourceId);
    DataTag getTagByName(EventTypeEnum tagDataTypeSystem,  int configurationId, String name);
    DataTag getTagById(Integer id);
    List<AlarmBuffer> getAllBuffers();
    List<AlarmCategory> getAllCategories();
    List<CategorySignalMap>  getAllMapCategoryEvents();
    Vehicle getVehicle(String vid);
    void addTagDescription(AlarmTagDescription alarmTagDescription);
    AlarmTagDescription getTagAlarmDescription(String language, Integer tagId);
    boolean sendVncStopMessage(String vehicleId, Integer port);
    boolean sendVncStartMessage(String vehicleId, Integer port);
    void addDCParserAckNackListener(DCParserAckNackEventHandler handler);
    void removeDCParserAckNackListener(DCParserAckNackEventHandler handler);
    ReturnCode changeUserPassword(User currentUser, String password, String newPasswordStr, String newPasswordStr2);
    ReturnCode setPassword(User myUser, String newPassword);
    ReturnCode importAlarms(Vehicle vehicle, IDiagnosticsEventHandler listener, File zipFile);
    AlarmCategory getCategory(Integer categoryId);

    DataTag getTagByProcessId(Integer processId);

    <T> List<T>  loadListFromXml(Class<T> clazz);
}
