package co.luism.lukisoftiot.common.datatypes;/*
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
 * datacollector
 * co.luism.lukisoftiot.common.datatypes
 * Created by luis on 19.09.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */
public class PercentageValue extends UnsignedInteger {


    public PercentageValue(int value) {
        super(value);

        if(value > 100) {
            throw new NumberFormatException("value is above 100%");
        }
    }
}
