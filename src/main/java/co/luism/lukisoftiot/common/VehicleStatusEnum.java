package co.luism.lukisoftiot.common;

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

/**
 * webmanager
 * co.luism.lukisoftiot.datacollector.common
 * Created by luis on 03.10.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */
public enum VehicleStatusEnum {

    ST_NONE(0),
    ST_OFFLINE(1), ST_ONLINE(2), ST_SUSPENDED(3);

    private final int value;

    private VehicleStatusEnum(int value) {
        this.value = value;


    }

    public int getValue() {
        return value;
    }
}
