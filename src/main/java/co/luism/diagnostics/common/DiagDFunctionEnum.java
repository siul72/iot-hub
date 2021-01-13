package co.luism.diagnostics.common;

/**
 * Created by luis on 21.10.14.
 */
public enum DiagDFunctionEnum {

    FC_GET_NEXT_EVENT("<getNewEvents>"),
    FC_GET_NEXT_EVENT_RESPONSE("<getNewEvents"), FC_ACK("<ack>"),
    FC_NO_MORE_EVENTS("ENOACTIVEEVENTS"), FC_GET_ENV_DATA("<getEnvData"),
    FC_ENV_DATA_END("END"), FC_ENV_DATA_POINTER_ERROR("P_ERROR"),
    FC_ENV_DATA_NO_DATA_FOUND("ENOTFOUND"), FC_ENV_DATA_DATA("DATA");


    private final String value;

    DiagDFunctionEnum(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }

    public static DiagDFunctionEnum getName(String value){
        for(DiagDFunctionEnum f : DiagDFunctionEnum.values()){
            if(f.value.equals(value)){
                return f;
            }
        }

        return null;
    }
}
