package co.luism.lukisoftiot.datacollector;

import co.luism.lukisoftiot.enterprise.Vehicle;
import org.apache.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by luis on 21.10.14.
 */
public class DataCollectorTimer {

    private static final Logger LOG = Logger.getLogger(DataCollectorTimer.class);
    private final Timer timer;
    private final Vehicle myVehicle;

    public DataCollectorTimer(Vehicle v, int seconds) {
        timer = new Timer();
        myVehicle = v;
        timer.schedule(new TimeOutTask(), seconds*1000);
        LOG.info(String.format("timer start with %d timeout", seconds));
    }

    class TimeOutTask extends TimerTask {
        public void run() {
            LOG.info(String.format("Time's up for vehicle %s!", myVehicle.getVehicleId()));
            myVehicle.timerTimeOut();
            timer.cancel();
            timer.purge();
        }
    }

    public void cancelTimer(){
        if(myVehicle != null) {
            LOG.info(String.format("Timer cancel for vehicle %s", myVehicle.getVehicleId()));
        }
        timer.cancel();
        timer.purge();
    }

}

