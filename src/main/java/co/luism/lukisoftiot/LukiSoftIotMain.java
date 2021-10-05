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
 * @file main.cpp
 * @brief
 * @author L. Coelho
 * @date 2014-01-29
 */
package co.luism.lukisoftiot;


import picocli.CommandLine;


import java.util.logging.Logger;


/**
 *
 *  ___       ____  ____  __   ___   __      ________   ______    _______  ___________
 * |"  |     ("  _||_ " ||/"| /  ") |" \    /"       ) /    " \  /"     "|("     _   ")
 * ||  |     |   (  ) : |(: |/   /  ||  |  (:   \___/ // ____  \(: ______) )__/  \\__/
 * |:  |     (:  |  | . )|    __/   |:  |   \___  \  /  /    ) :)\/    |      \\_ /
 *  \  |___   \\ \__/ // (// _  \   |.  |    __/  \\(: (____/ // // ___)      |.  |
 * ( \_|:  \  /\\ __ //\ |: | \  \  /\  |\  /" \   :)\        / (:  (         \:  |
 *  \_______)(__________)(__|  \__)(__\_|_)(_______/  \"_____/   \__/          \__|
 *
 * @author luis
 *
 */
class LukiSoftIotMain {

    LukiSoftIotMain(){

    }
	
	private static final Logger LOG = Logger.getLogger(LukiSoftIotMain.class.getSimpleName());

	/**
	 * @param args - args
	 * @throws InterruptedException 
	 */

    public static void main(String... args)  {
        CommandLine commandLine = new CommandLine(new LukiiotCommand());
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }





}
