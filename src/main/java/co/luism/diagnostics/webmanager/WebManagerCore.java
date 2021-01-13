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
package co.luism.diagnostics.webmanager;


import co.luism.diagnostics.common.DiagnosticsConfig;
import co.luism.diagnostics.common.Utils;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;


/**
 * @author luis
 *
 */
class WebManagerCore {

    WebManagerCore(){

    }
	
	private static final Logger LOG = Logger.getLogger(WebManagerCore.class);

	/**
	 * @param args - args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {

        // create Options object
        Options options = new Options();
        // add t option
        options.addOption("v", false, "display version");
        options.addOption("r", false, "run it");
        options.addOption("c",true, "create database");

        CommandLine line = null;

        // create the parser
        CommandLineParser parser = new GnuParser();
        try {
            // parseStringToBigDecimal the command line arguments
            line = parser.parse(options, args);
        }catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }

        if(args.length <= 0) {
            // automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "dataCollector", options );
            return;
        }

        try {
            if (line != null) {
                if (line.hasOption("v")) {
                    System.out.println(String.format("Version %s", DiagnosticsConfig.SW_VERSION));
                    return;
                }

                if (line.hasOption("c")) {

                    Utils.createSchema(line.getOptionValue("c"));
                }

                if (line.hasOption("r")) {
                    boolean runLoop = true;
                    WebManager myDCM = WebManager.getInstance();
                    myDCM.init(WebManager.class);

                    while(runLoop) {
                        Thread.sleep(1000);
                    }
                }
            }
        }catch (java.lang.NullPointerException e){
            System.out.println("Invalid parameters");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "dataCollector", options );
            return;
        }
	}




}
