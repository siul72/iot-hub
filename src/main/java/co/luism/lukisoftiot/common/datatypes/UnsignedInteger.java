package co.luism.lukisoftiot.common.datatypes;/*

  _  __ _____  ____  ______ _______
 | |/ // ____|/ __ \|  ____|__   __|
 | ' /| (___ | |  | | |__     | |
 |  <  \___ \| |  | |  __|    | |
 | . \ ____) | |__| | |       | |
 |_|\_\_____/ \____/|_|       |_|
    
*/

/**
 * datacollector
 * co.luism.lukisoftiot.common.datatypes
 * Created by luis on 19.09.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */
public class UnsignedInteger {

    private Integer value;

    public UnsignedInteger(int value) {
       if (value < 0){
           throw new NumberFormatException("value is below zero");
       }

       this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        if (value < 0){
            throw new NumberFormatException("value is below zero");
        }
        this.value = value;
    }
}
