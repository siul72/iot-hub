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

package co.luism.datacollector;

import co.luism.diagnostics.common.WatchDogClient;
import co.luism.datacollector.common.DataCollectorClient;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.ConcurrentModificationException;

/**
 * Created by luis on 04.11.14.
 */
public class DCSocketKeepAliveCheck implements Runnable{

    private static final Logger LOG = Logger.getLogger(DCSocketKeepAliveCheck.class);
    private static boolean runLoop = true;
    private final DataCollectorSocket parent;
    private WatchDogClient myWatchDog = new WatchDogClient(DCSocketKeepAliveCheck.class.getSimpleName());

    DCSocketKeepAliveCheck(DataCollectorSocket parent){
        this.parent = parent;
    }

    @Override
    public void run() {
        LOG.info(DCSocketKeepAliveCheck.class.getSimpleName() + " Thread started");
        myWatchDog.init();
        myWatchDog.register(10);
        //every second check connections
        runLoop = true;
        while (runLoop){

            if(!myWatchDog.update()){
                myWatchDog.init();
                myWatchDog.register(10);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.error(e);
                if(!runLoop){
                    break;
                }
            }


            Collection<DataCollectorClient> s = parent.getClientSocketList().values();

            for(DataCollectorClient cli : s){
                if(!runLoop){
                    break;
                }

                Integer currentTimeOut;
                try {
                    currentTimeOut = cli.getCurrentTimeOut();

                    if(currentTimeOut == null){
                        //LOG.error("time out is null");
                        continue;
                    }

                    if(currentTimeOut > 0){
                        cli.decrementCurrentTimeOut();
                        continue;
                    }

                    //now currentTimeOut is 0 - Close the Connection
                    LOG.warn(String.format("Time Out reached for connection %s", cli.getIndex().toString()));
                    LOG.warn(String.format("No Life Signal Message after %d times", cli.getCloseTimeOut()));
                    parent.closeClient(cli);

                } catch (ConcurrentModificationException ex){
                    LOG.error(ex);
                    if(!runLoop){
                        break;
                    }
                }
            }
        }

        LOG.debug("exit from DCSocketKeepAliveCheck");
    }

    public void closeDown(){
        runLoop = false;
    }


}
