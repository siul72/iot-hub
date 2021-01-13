package co.luism.datacollector.messages;

import co.luism.diagnostics.common.DiagnosticsConfig;
import co.luism.diagnostics.common.EventTypeEnum;
import co.luism.diagnostics.enterprise.DataTag;
import co.luism.diagnostics.webmanager.WebManagerFacade;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luis on 05.11.14.
 */
public class DCLifeSign {

    private static final Logger LOG = Logger.getLogger(DCLifeSign.class);
    private static final Map<Integer, DataTag> mapParameterDataTag = new HashMap<>();
    private static DCLifeSign instance;

    public static DCLifeSign getInstance(){

        if(instance == null){
            instance = new DCLifeSign();
        }

        return instance;
    }

    DCLifeSign(){
        boolean created = false;
        //populate the MAP
        Map<String, Object> myRestrictions = new HashMap<>();

        for(DCLifeSignParamEnum p : DCLifeSignParamEnum.values()){

            myRestrictions.put("type" , EventTypeEnum.TAG_DATA_TYPE_SYSTEM.getValue());
            myRestrictions.put("name", p.name());

            DataTag t = DataTag.read(DataTag.class, myRestrictions);

            if(t == null){
                t = new DataTag(EventTypeEnum.TAG_DATA_TYPE_SYSTEM, null,  null , p.name() );
                t.create();
                created = true;
            }

            if(p == DCLifeSignParamEnum.LATITUDE || p == DCLifeSignParamEnum.LONGITUDE){
                t.setScale(DiagnosticsConfig.GPS_COORDINATES_SCALE);
            }

            mapParameterDataTag.put(p.ordinal(), t);
        }

        if(created){
            LOG.info("Reloading DataTags...");
            WebManagerFacade.getInstance().reloadDataTags();
        }

        LOG.info("... done create System Tags");

    }

    public DataTag get(Integer paramIndex){
        return mapParameterDataTag.get(paramIndex);
    }
}
