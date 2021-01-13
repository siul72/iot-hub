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

package co.luism.diagnostics.interfaces;

import co.luism.diagnostics.webmanager.WebManager;
import org.apache.log4j.Logger;


/**
 * Created by luis on 08.09.14.
 */
public class WebManagerStartJob implements Runnable {

    private boolean runLoop = true;
    private static final Logger LOG = Logger.getLogger(WebManagerStartJob.class);
    private Class resourceClass;

    public WebManagerStartJob(Class clazz)
    {
        resourceClass = clazz;
    }

    @Override
    public void run() {

        WebManager myDCM = WebManager.getInstance();
        myDCM.init(resourceClass);

        LOG.info(WebManagerStartJob.class.getSimpleName()+ " Thread started");
        runLoop = true;
        while (runLoop) {

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
                myDCM.close();
            }
        }
        
        LOG.info("runnable DataCollectorManager about stop");
        myDCM.close();
    }

    public void stop(){
    	LOG.info("runnable DataCollectorManager ask to stop");
        this.runLoop = false;
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
            LOG.error(e.getMessage());
		}
    }
}
