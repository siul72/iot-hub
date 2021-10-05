package co.luism.lukisoftiot.common;


import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;
import java.io.File;

@XmlRootElement(name = "Config")
public final class DiagnosticsConfig extends XmlObject {
    private static final Logger LOG = Logger.getLogger(DiagnosticsConfig.class);
    private static final String xmlFile = DiagnosticsConfig.FILE_CONFIG_PATH + File.separator + DiagnosticsConfig.class.getSimpleName() +".xml";

    public static final String SW_VERSION = "0.01.00";
    public static final int SUSPEND_MESSAGE_SIZE = 1;
    public static final int SUSPEND_MESSAGE_TIMEOUT_POS = 4;
    public static final int SUSPEND_MAX_TIMEOUT = 180;
    public static final int MSG_RESUME_VEHICLE_ID_POS = 4;
    public static final int DIAGD_REQUEST_DATA_PARAM_SIZE = 1;
    public static final int MSG_DIAGD_COMMAND_POS = 4;
    public static final int MSG_DIAGD_DATA_POS = 5;
    public static final String DEFAULT_FLEET_NAME = "F-1";
    public static final String DAIGD_EVENT_STRING_SPLITER = ",";
    public static final int DIAGD_EVENT_SIZE = 8;
    public static final int DAIGD_EVENT_REF_POS = 6;
    public static final int DAIGD_EVENT_START_TS_POS = 1;
    public static final int DAIGD_EVENT_START_TS_MS_POS = 2;
    public static final int DAIGD_EVENT_END_TS_POS = 3;
    public static final int DAIGD_EVENT_END_TS_MS_POS = 4;
    public static final int DAIGD_EVENT_INDEX_POS = 0;
    public static final int DAIGD_EVENT_NUMBER_OF_EVENTS_POS = 5;
    public static final int DIAGD_EVENT_STATUS_POS = 7;
    public static final long DIAGD_EVENT_PULL_DELAY = 3000;
    public static final int MSG_DIAGD_ENVDATA_INDEX_POS = 5;
    public static final int MSG_DIAGD_ENVDATA_CATEGORY_INDEX_POS = 6;
    public static final int MSG_DIAGD_ENVDATA_STATUS_POS = 7;
    public static final Integer EXTRA_TIMEOUT_VALUE = 10;
    public static final Integer DEFAULT_CLOSE_TIMEOUT = 3;
    public static final double GPS_COORDINATES_SCALE = 0.0000001;
    public static final long WEB_V_LOADER_DELAY = 10;
    public static final int DC_DEFAULT_EXTRA_TIME_GET_ENV_DATA = 3000;
    public static final Integer ENV_DATA_REQUEST_COUNTER = 3;
    public static final int STOP_VNC_SERVER_SIZE = 1;
    public static final int START_VNC_SERVER_SIZE = 1;
    public static final int MSG_ACK_FUNCTION_POS = 4;
    public static final int MSG_DIAGD_ENVDATA_DATA_TS_POS = 8;
    public static final int MSG_DIAGD_ENVDATA_DATA_TS_MS_POS = 9;
    public static final int MSG_DIAGD_ENVDATA_DATA_DATA_CHUNK_POS = 10;
    public static final int MSG_RESUME_LS_TIMEOUT_POS = 5;
    public static final Integer DEFAULT_ALARM_PULL_COUNT = 15; //seconds
    public static final Integer FAST_ALARM_PULL_COUNT = 1;
    public static final String DEFAULT_FLEET_ICON = "default_fleet";
    public static final int DC_DEFAULT_MS_VALUE = 0;
    public static final int TAG_SYSTEM_DEFAULT_CONFIG_ID = 0;
    public static final int MSG_HELLO_DIAG_INIT_POS = 12;
    public static final int MAX_BACK_THREADS = 5;
    public static final int CONFIGURE_PROCESS_DATA_SIZE = 2;

    private static DiagnosticsConfig instance = null;
	public static final String EXIT_STRING = "EXIT";
	public static final int RCV_BUFFER_DELAY = 10;
	public static final String FrameSplitChar = ";";
    public static final String PROCESS_DATA_SPLITTER = ":";
    public static final String PD_OFFLINE_BUFFER_SPLITTER = ",";
    public static final int MESSAGE_HEADER_SIZE = 4;
    public static final int MSG_HEADER_N_PARAMS_POS = 1;
	public static final int MSG_HEADER_TIME_STAMP_POS = 2;
   	public static final int MSG_HEADER_SEQUENCE_NUMBER_POS = 3;
	public static final int HELLO_MESSAGE_SIZE = 7;
	public static final int MSG_HELLO_VEHICLE_ID_POS = 5;
	public static final int MSG_HELLO_SMS_NUMBER_POS = 6;
    public static final int MSG_HELLO_VEHICLE_LS_TIMEOUT_POS = 10;
	public static final int NACK_PARAM_SIZE = 3;
	public static final int ACK_PARAM_SIZE = 2;
	public static final int LIFE_SIGN_MESSAGE_SIZE = 3;
	public static final int MSG_LIFE_SIGN_LAT_POS = 4;
	public static final int MSG_LIFE_SIGN_LONG_POS = 5;
	public static final int MSG_LIFE_SIGN_STATUS_POS = 6;
    public static final int MSG_LIFE_SIGN_PD_BUFFER_POS = 9;
    public static final String FILE_CONFIG_PATH = "config";
    public static final int START_PROCESS_DATA_PARAM_SIZE = 1;
    public static final int PROCESS_DATA_UPDATE_INTERVAL = 1;
    public static final int STOP_PROCESS_DATA_PARAM_SIZE = 1;
    public static final int PROCESS_DATA_VALUE_PARAM_SIZE = 2;
    public static final int PROCESS_DATA_VALUE_PARAM_ID_POS = 0;
    public static final int PROCESS_DATA_VALUE_PARAM_VALUE_POS = 1;
    public static final int EVENT_DATA_VALUE_PARAM_SIZE = 3;
    public static final int EVENT_DATA_VALUE_PARAM_ID_POS = 0;
    public static final int EVENT_DATA_VALUE_PARAM_VALUE_POS = 1;
    public static final int EVENT_DATA_VALUE_PARAM_TIMESTAMP_POS = 2;
    public static final int MSG_DIAGD_SIZE = MESSAGE_HEADER_SIZE +1;

    private int serverPort = 51313;
    private String version ="0.0.1-SNAPSHOT";
    private String salt;

    private DiagnosticsConfig(){


    }

    public static <T> DiagnosticsConfig setInstance(Class clazz) {
        if(instance == null){
            DiagnosticsConfig.init(clazz);


        }
        return instance;
    }

    public static DiagnosticsConfig getInstance() {

        return instance;
    }

    private static void createConfigFile(File f){

        try {
            instance = new DiagnosticsConfig();
            instance.toXml(f);

        } catch (JAXBException e) {

            LOG.error(e.getMessage());
        }
    }


    private static <T> void init(Class clazz){

        File f = Utils.getResourceFile(clazz, xmlFile);
        LOG.info(String.format("get config from %s %s", f.getPath(), f.getName()));
        if(f == null){
            LOG.info("Config file is null");
            return;
        }else{

            if(!f.exists()){
                LOG.info("Config file not found, create a new one");
                createConfigFile(f);
            } else{
                try{
                    instance=null;
                    instance = DiagnosticsConfig.fromXml(DiagnosticsConfig.class);


                } catch (JAXBException e) {
                    LOG.error("JAXBException :" + e.getMessage());
                }

            }

        }
    }

    public String getVersion() {
        return version;
    }

    @XmlAttribute
    public void setVersion(String id) {
        this.version = id;
    }

    public int getServerPort() {
        return serverPort;
    }

    @XmlElement
    public void setServerPort(int serverPort) {

        this.serverPort = serverPort;
    }

    public String getSalt() {
        return salt;
    }

    @XmlElement
    public void setSalt(String salt) {
        this.salt = salt;
    }


}
