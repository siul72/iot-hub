package co.luism.diagnostics.common;

public enum FunctionEnum {
	
	FC_NONE(0), FC_NACK(1), FC_ACK(2),
	FC_HELLO(3), FC_LIFE_SIGN(4), FC_START_PROCESS_DATA(5),
	FC_STOP_PROCESS_DATA(6), FC_PROCESS_DATA(7),
	FC_EVENT_DATA(8), FC_SUSPEND_CONNECTION(9), FC_RESUME_CONNECTION(10),
    FC_DIAGD_REQUEST(11), FC_DIAGD_RESPONSE(12), FC_START_VNC_SERVER(13),
    FC_STOP_VNC_SERVER(14), FC_CONFIGURE_PROCESS_DATA(15);
    

    private final int value;

    private FunctionEnum(int value) {
        this.value = value;


    }

    public int getValue() {
		return value;
	}
     
    public static FunctionEnum getFunction(int ix){
        for(FunctionEnum f : FunctionEnum.values()){
            if(f.getValue() == ix){
                return f;
            }
        }

        return null;
    }

}
