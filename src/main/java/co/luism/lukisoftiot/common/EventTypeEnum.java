package co.luism.lukisoftiot.common;



public enum EventTypeEnum {

    TAG_DATA_TYPE_NONE(0), TAG_DATA_TYPE_PD(1), TAG_DATA_TYPE_EVENT(2),
    TAG_DATA_TYPE_SYSTEM(3), ALARM_REFRESH(4), TAG_DATA_HISTORY_REFRESH(5),
    TAG_DATA_HISTORY_NEW_DATA(6), TAG_DATA_HISTORY_POSITION_REFRESH(7), PROGRESS_IMPORT_ALARM_DATA(8),
    PROGRESS_IMPORT_ENV_DATA(9);


    private final int value;

    private EventTypeEnum(int value) {
        this.value = value;


    }

    public int getValue() {
        return value;
    }

    public static EventTypeEnum getEnum(int i){

        for(EventTypeEnum e : EventTypeEnum.values()){
            if(e.getValue() == i){
                return e;
            }
        }

        return null;

    }



}