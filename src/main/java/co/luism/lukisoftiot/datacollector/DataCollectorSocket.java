package co.luism.lukisoftiot.datacollector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import co.luism.lukisoftiot.datacollector.common.DataCollectorClient;
import co.luism.lukisoftiot.common.DiagnosticsConfig;
import co.luism.lukisoftiot.common.VehicleStatusEnum;
import co.luism.lukisoftiot.enterprise.Vehicle;

import org.apache.log4j.Logger;


final public class DataCollectorSocket {
	

	private static final Logger LOG = Logger.getLogger(DataCollectorSocket.class);
    private final Map<String, DataCollectorClient> clientSocketList= new ConcurrentHashMap<>();
    private ServerSocket socketServer=null;
    private final int port;
	private static DataCollectorSocket instance = null;
	private static boolean runLoop = true;
    private static DCSocketKeepAliveCheck myKeepAlive;
    private static Thread myKeepAliveThread;
    private static Thread myDataScannerThread;
    private static ServerThread myServerThread;


    private DataCollectorSocket(){
        this(6052);
    }

    private DataCollectorSocket(int port){
		 
		this.port = port;
	}
	
	static public void setInstance(int port){
		if (instance == null){
			instance = new DataCollectorSocket(port);
		}
	}
	
	static public DataCollectorSocket getInstance(){
		return instance;
	}
	
	public void init(){

        myServerThread = new ServerThread();
        myServerThread.setName(ServerThread.class.getSimpleName());
        myServerThread.start();

        new Thread(DataCollectorBuffer.getInstance()).start();

        myKeepAlive = new DCSocketKeepAliveCheck(DataCollectorSocket.getInstance());
        myKeepAliveThread = new Thread(myKeepAlive);
        myKeepAliveThread.setName(DCSocketKeepAliveCheck.class.getSimpleName());
        myKeepAliveThread.start();
        DataCollectorDataScanner.getInstance().init();
        myDataScannerThread = new Thread(DataCollectorDataScanner.getInstance());
        myDataScannerThread.setName(DataCollectorDataScanner.class.getSimpleName());
        myDataScannerThread.start();
 
	}

    public String getGUID(String vehicleID) {
        for(DataCollectorClient c : this.clientSocketList.values()){

            if(c.getMyVehicle() == null){
                continue;
            }

            if(c.getMyVehicle().getVehicleId().equals(vehicleID)){
                return c.getIndex().toString();
            }


        }

        return null;

    }

    public Map<String, DataCollectorClient> getClientSocketList() {
        return clientSocketList;
    }

    public boolean isVehicle(String guId) {

        DataCollectorClient c = this.clientSocketList.get(guId);

        if(c == null){
            LOG.error("Client socket not found " + guId);
            return false;
        }

        return c.getMyVehicle() != null;

    }

    public Vehicle findVehicleByGuId(String guId) {

        DataCollectorClient c = this.clientSocketList.get(guId);
        if(c == null){
            return null;
        }

        return  c.getMyVehicle();
    }

    public Set<DataCollectorClient> getConnectedVehicleSet(){
        Set<DataCollectorClient> mySet = new HashSet<>();

        for(DataCollectorClient c : clientSocketList.values()){
            if(c.getMyVehicle() != null){
                mySet.add(c);
            }
        }

        return mySet;
    }

    public void refreshCountDown(String guId) {

        DataCollectorClient cli = getClientByGuid(guId);

        if(cli == null){
            LOG.error("cli not found");
            return;
        }

        //LOG.info(String.format("connection timeout refresh %d", cli.getCloseTimeOut()));
        cli.resetCurrentTimeOut();
    }

    public void setGetNewEventLow(String guId, boolean b) {
        DataCollectorClient c = this.getClientSocketList().get(guId);
        if(c == null){
            return;
        }

        if(b){
            if(c.getConfiguredAlarmPullCount().equals(DiagnosticsConfig.DEFAULT_ALARM_PULL_COUNT)){
                c.setConfiguredAlarmPullCount(DiagnosticsConfig.DEFAULT_ALARM_PULL_COUNT);
                LOG.debug("Received no more events set timer to low");
            }
        } else {
            if(c.getConfiguredAlarmPullCount().equals(DiagnosticsConfig.FAST_ALARM_PULL_COUNT)){
                c.setConfiguredAlarmPullCount(DiagnosticsConfig.FAST_ALARM_PULL_COUNT);
                LOG.debug("Received new events set timer to fast");
            }
        }
    }

    private class ServerThread extends Thread{

        @Override
        public void run() {
            LOG.info(this.getName() + " Thread started");
            clientSocketList.clear();
            runLoop = true;
            while(runLoop){
                try{
                    if(socketServer == null){
                        LOG.info(String.format("Try Listening on port %d ......", port));
                        socketServer = new ServerSocket();
                        socketServer.setReuseAddress(true);
                    }

                    if(!socketServer.isBound()){
                        socketServer.bind(new InetSocketAddress(port));
                    }

                    if(socketServer != null){
                        LOG.debug("Accept New Connections on " + socketServer.getLocalPort());
                        Socket s= socketServer.accept();
                        LOG.info(String.format(String.format("Received connection from %s", s.getRemoteSocketAddress().toString())));
                        DataCollectorClient myClient = new DataCollectorClient(s);
                        ClientThread ct = new ClientThread(myClient);
                        ct.setName(myClient.getIndex().toString());
                        ct.start();
                        clientSocketList.put(myClient.getIndex().toString(), myClient);
                        LOG.info(String.format("We have now %d active connections!", clientSocketList.size()));

                    } else {
                        LOG.error(String.format("Not able to use port %d, maybe already in use", port));
                    }

                } catch(Exception e){

                    LOG.error("Connection Error:" + e);
                } finally {

                    if(!runLoop){
                        LOG.error("Close Server Listener");

                    }

                    try {
                        Thread.sleep(3 * 1000);
                    } catch (InterruptedException e) {
                      LOG.error(e);
                    }
                }
            }
        }
    }
 

	class ClientThread extends Thread{  

	    String line=null;
	    BufferedReader  is = null;
	    PrintWriter os=null;
	    DataCollectorClient c=null;

	    public ClientThread(DataCollectorClient c){
	        this.c=c;
	    }

        @Override
	    public void run(){
            LOG.info(this.getName() + " Thread started");
            BlockingQueue<String> buffer = DataCollectorBuffer.getInstance().getBuffer();
	       //String address =( (InetSocketAddress)s.getRemoteSocketAddress()).getHostName();
	       	      
	    	try{

	    		is= new BufferedReader(new InputStreamReader(c.getSocket().getInputStream()));
	    		os=new PrintWriter(c.getSocket().getOutputStream());

	    	}catch(IOException e){
	    		LOG.error("IO error in client thread " + e);
	    	}

	    	try{
	    		runLoop = true;
	    		while(runLoop)
	    		{
         			line=is.readLine();
		            if(line == null)
		            {
		            	LOG.debug("read null >>> exit");
		            	break;
		            }
		            
		            try {
		            	
		            	String frame = String.format("%s@%s", c.getIndex().toString(), line);
						buffer.put(frame);
					} catch (InterruptedException e) {

                        LOG.error(e);
                    }

	    		} 
	    		 
	    		
	    	} catch (IOException e) {

	    		line=this.getName(); //reused String line for getting thread name
	    		LOG.error("Client " + line + " terminated " + e);
	    	}catch(NullPointerException e){
	    		line=this.getName(); //reused String line for getting thread name
	    		LOG.error("Client " + line + " Closed " + e);
	    	} finally{
			    try{
			    	LOG.info(String.format("Connection Closing from %s", c.getSocket().getRemoteSocketAddress().toString()));
			    	 
			        if (is!=null){

			            is.close(); 
			            //LOG.debug(" Socket Input Stream Closed");
			        }
		
			        if(os!=null){
                        os.flush();
			            os.close();
			            //LOG.debug("Socket Out Stream Closed");
			        }

			        if (c.getSocket()!=null){
			        	c.getSocket().close();
			        //LOG.debug("Socket Closed");
			        }
		
			    }catch(IOException ie){
			    	LOG.debug("Socket Close Error " + ie);
			    }
			    
                removeVehicleConnection(c);

			    LOG.info(String.format("We have now %d active connections!", clientSocketList.size()));
	    	}//end finally

            LOG.info(this.getName() + " Thread stop");
	    }
	}
	
	 

	public void close() {
		LOG.debug("Close Sockets");
		runLoop = false;

        myKeepAlive.closeDown();
        DataCollectorDataScanner.getInstance().closeDown();
        DataCollectorBuffer.getInstance().stopThread();

		//close all client connections
		for(DataCollectorClient c :  clientSocketList.values()){
            removeVehicleConnection(c);
			Socket s = c.getSocket();
			if(s.isConnected())
			{
				try {

					s.close();
				} catch (IOException e) {

                    LOG.error(e);
				}
			}
		}
		
		clientSocketList.clear();

		try {
            if(this.socketServer != null){
                this.socketServer.close();
            }

		} catch (IOException e) {
	        LOG.error(e);
		}

        socketServer = null;

    }
	
	public DataCollectorClient getClientByGuid(String guid){

        return this.clientSocketList.get(guid);

	}
	
	public DataCollectorClient isOnAnotherConnection(String guId, String vID){
		
		for(DataCollectorClient c : this.clientSocketList.values()){
			
			if(guId.equals(c.getIndex().toString())){
				continue;
			}
			
			if(c.getMyVehicle() == null){
				continue;
			}
			
			if(vID.equals(c.getMyVehicle().getVehicleId() )){
				return c;
			}
 	
		}
		
		return null;
		
	}

	public boolean sendFrame(String guId, String frame) {


        DataCollectorClient c = this.clientSocketList.get(guId);

        if(c == null){

            LOG.error(String.format("DataCollectorClient not found for connection %s", guId));
            return false;

        }


        return sendFrame(c, frame);


	}

    public boolean sendFrame(DataCollectorClient c, String frame) {

        if(c == null){

            LOG.error(String.format("DataCollectorClient is null"));
            return false;

        }

        boolean sendFlag = false;

        //append # to the frame;
        frame = frame.concat("#");

        Socket s = c.getSocket();

        if(!s.isConnected() && s.isClosed()){
            LOG.error("Socket is not connected");
            return sendFlag;
        }

        //TODO - investigate this pointer
        PrintWriter os;
        try {
            os = new PrintWriter(s.getOutputStream());

            //os.write(frame);
            os.println(frame);
            os.flush();
            sendFlag = true;
        } catch (IOException e) {

            LOG.error("Socket error:" + e);
        }

        if(!sendFlag){
            LOG.info("No socket found to send frame");
        }

        LOG.debug("#### TX frame:" + frame);
        return sendFlag;
    }

    public boolean updateVehicle(String guId, Integer closeTimeOut, Vehicle v) {

        DataCollectorClient cli = this.clientSocketList.get(guId);

        if(cli == null){

            LOG.error(String.format("client not found %s", guId));
            return false;

        }

        cli.setCloseTimeOut(closeTimeOut);
        return updateVehicle(cli,  v);

    }


    private boolean updateVehicle(DataCollectorClient cli, Vehicle v) {


        try{
            cli.setMyVehicle(v);

        } catch (NullPointerException ex){
            LOG.error("null pointer on set vehicle" + ex);
            return false;
        }

        return true;

    }

	public void closeClient(String guId) {

        DataCollectorClient cli = this.clientSocketList.get(guId);

        if(cli == null){

            LOG.error(String.format("client not found %s", guId));
            return;

        }

        closeClient(cli);


		
	}

    public void closeClient(DataCollectorClient cli) {
        if(cli == null){

            LOG.error(String.format("client is null"));
            return;
        }

        if(!this.getClientSocketList().containsValue(cli)){
            LOG.warn("the cli was removed");
            return;
        }

        try {
            cli.getSocket().close();
        } catch (IOException e) {
            LOG.error(e);
        } finally {
            LOG.info("Client socket was active closed");
            removeVehicleConnection(cli);
        }
    }

    private void removeVehicleConnection(DataCollectorClient socketClient){

        Vehicle currentVehicle = socketClient.getMyVehicle();

        if(currentVehicle != null){

            if(currentVehicle.getStatus() != VehicleStatusEnum.ST_SUSPENDED){
                currentVehicle.setStatus(VehicleStatusEnum.ST_OFFLINE);
            } else {
                LOG.debug(String.format("Vehicle %s was suspended", currentVehicle.getVehicleId()));
            }

            socketClient.setMyVehicle(null);

        }

        this.clientSocketList.remove(socketClient.getIndex().toString());
    }
}
