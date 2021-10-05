/*
  ___       ____  ____  __   ___   __      ________   ______    _______  ___________
|"  |     ("  _||_ " ||/"| /  ") |" \    /"       ) /    " \  /"     "|("     _   ")
||  |     |   (  ) : |(: |/   /  ||  |  (:   \___/ // ____  \(: ______) )__/  \\__/
|:  |     (:  |  | . )|    __/   |:  |   \___  \  /  /    ) :)\/    |      \\_ /
 \  |___   \\ \__/ // (// _  \   |.  |    __/  \\(: (____/ // // ___)      |.  |
( \_|:  \  /\\ __ //\ |: | \  \  /\  |\  /" \   :)\        / (:  (         \:  |
 \_______)(__________)(__|  \__)(__\_|_)(_______/  \"_____/   \__/          \__|


(c) 2021
*/

package co.luism.lukisoftiot.datacollector;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import co.luism.lukisoftiot.common.WatchDogClient;
import co.luism.lukisoftiot.common.DiagnosticsConfig;
import org.apache.log4j.Logger;


/**
 * DataCollectorBuffer is the class to handle received data from the data clients
 * Its main functions are
 * <ul> Receive the string data, send it to the DataCollectorParser
 * <p>
 *
 * @author      L. Coelho
 * @version     %I%, %G%
 * @since       1.0
 */
public final class DataCollectorBuffer implements Runnable {
	private static DataCollectorBuffer instance = null;
	private boolean runLoop = true;
	private final BlockingQueue<String> buffer = new LinkedBlockingQueue<>(1000);
	private static final Logger LOG = Logger.getLogger(DataCollectorBuffer.class);
    private static String name = DataCollectorBuffer.class.getSimpleName();
    private WatchDogClient myWatchDog = new WatchDogClient(name);

    /**
     * private constructor for singleton use
     * It calls start method to init the process
     */
    private DataCollectorBuffer() {
        

	}

    /**
     * Get the singleton reference to the unique object
     * @return DataCollectorBuffer
     */
	static public DataCollectorBuffer getInstance() {

		if (instance == null) {
			instance = new DataCollectorBuffer();
		}

		return instance;
	}

    public String getName() {
        return name;
    }

    /**
     * Get the reference to the buffer in order to put strings there
     * @return  reference to the buffer
     */
	public BlockingQueue<String> getBuffer() {
		return buffer;
	}

    /**
     * run method override from thread class
     * this method is called when invoked the start() method
     */
    @Override
	public void run() {
		LOG.info(this.getName() + " Thread started");
        myWatchDog.init();
        myWatchDog.register(20);
        runLoop = true;
		while (runLoop) {
            if(!myWatchDog.update()){
                myWatchDog.init();
                myWatchDog.register(20);
			}
			try {
                String m = buffer.poll(10, TimeUnit.SECONDS);

                if(m==null){

                    if (!runLoop) {
                        break;
                    }

                    continue;
                }
				parse(m);
			} catch (InterruptedException e) {

                LOG.error("unable to take data:" + e.getMessage());

                if (!runLoop) {
					break;
				}
			}

			try {
				Thread.sleep(DiagnosticsConfig.RCV_BUFFER_DELAY);
			} catch (InterruptedException e) {

                LOG.error("sleep was interrupted:" + e.getMessage());
				if (!runLoop) {
					break;
				}
			}
		}

		LOG.info("Buffer Thread stop");
	}

    /**
     * method to stop thread and close the object
     */
	public void stopThread() {
		runLoop = false;
		try {
			buffer.put(DiagnosticsConfig.EXIT_STRING);
		} catch (InterruptedException e) {


            LOG.error("unable to put exit string:" + e.getMessage());
		}
	}

    /**
     * Method to parseStringToBigDecimal the buffered string received
     * @param s - the string representation of the data
     */
	private void parse(String s) {
		if (s == null) {
			LOG.info("no data in the queue");
			return;
		}

		LOG.debug("#### RX frame:" + s);
		if(DiagnosticsConfig.EXIT_STRING.equals(s)){
			LOG.info("Received exit string");
			return;
		}
		
		String[] vehicleMessage = s.split("@", 2);

        if(vehicleMessage.length < 2){
            LOG.error("received frame with incorrect size");
            return;
        }

		String vehicleAddress = vehicleMessage[0];
		String frame = vehicleMessage[1];
		
		DataCollectorParser.getInstance().parse(vehicleAddress, frame);

	}

}
