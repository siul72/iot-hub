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

package co.luism.lukisoftiot.common;

import co.luism.lukisoftiot.enterprise.Vehicle;

import java.util.EventObject;
import java.util.List;

/**
 * Created by luis on 08.09.14.
 */
public class DiagnosticsEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private int timeStamp;
    private EventTypeEnum tagType;
    private List<Integer> listOfUpdatedItems = null;
    private Vehicle currentVehicle = null;
    private Float value;

    public DiagnosticsEvent(Object source) {
        super(source);
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public EventTypeEnum getTagType() {
        return tagType;
    }

    public void setTagType(EventTypeEnum tagType) {
        this.tagType = tagType;
    }

    public List<Integer> getListOfUpdatedItems() {
        return listOfUpdatedItems;
    }

    public void setListOfUpdatedItems(List<Integer> listOfUpdatedItems) {
        this.listOfUpdatedItems = listOfUpdatedItems;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    public void setCurrentVehicle(Vehicle currentVehicle) {
        this.currentVehicle = currentVehicle;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }
}
