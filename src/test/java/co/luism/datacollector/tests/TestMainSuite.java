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

package co.luism.datacollector.tests;

import co.luism.diagnostics.common.DateTimeUtils;
import co.luism.datacollector.DataCollectorBuffer;
import co.luism.datacollector.messages.DCLifeSignParamEnum;
import co.luism.diagnostics.common.Utils;
import co.luism.diagnostics.common.DiagnosticsConfig;
import co.luism.diagnostics.common.EventTypeEnum;
import co.luism.diagnostics.common.ReturnCode;
import co.luism.diagnostics.common.VehicleSyncStatusEnum;
import co.luism.diagnostics.enterprise.*;
import co.luism.diagnostics.webmanager.LanguageManager;
import co.luism.diagnostics.webmanager.WebManager;
import co.luism.diagnostics.webmanager.WebManagerFacade;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.log4j.Logger;
import org.fluttercode.datafactory.impl.DataFactory;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Created by luis on 05.09.14.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestMainSuite extends TestCase {

    private static final Logger LOG = Logger.getLogger(TestMainSuite.class);
    private static String globalVid = "12345678901234567890123456789012";

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public TestMainSuite( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestMainSuite.class );
    }

    private void sendHelloMessage(TCPClient cli, Integer seqN, Integer timeout,String initStatus) {


        //send hello message
        cli.write(String.format("3;9;%d;%d;0;%s;0;0;CH;0;%d;0;%s;",
                DateTimeUtils.getCurrentTimeStampSeconds(), seqN, globalVid, timeout, initStatus));
    }

    class TCPClient{
        private boolean isFinished = false;
        private Socket clientSocket;
        private PrintWriter outToServer;
        private BufferedReader  rcv;

        public boolean init(int port){
            try {
                clientSocket = new Socket("localhost", port);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            try {
                outToServer = new PrintWriter(clientSocket.getOutputStream());
                rcv= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch(IOException e){
                LOG.error("IO error in client thread " + e);
                return false;
            }

            return true;
        }

        public boolean write(String data){
            outToServer.println(data);
            outToServer.flush();
            return true;
        }

        public String readLine(){

            return readLine(3000);

        }

        public String readLine(final int timeout){

            TimerTask ft = new TimerTask(){
                public void run(){
                    if (!isFinished){
                        LOG.debug("dint read anything in " + timeout);
                        close();

                    }
                }
            };

            (new Timer()).schedule(ft,timeout);

            isFinished = false;
             try {

                 String msg = rcv.readLine();
                 isFinished = true;
                 return msg;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        public boolean isConnected(){

            return (readLine(1000)!=null);
        }

        public void close(){
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendLifeSigns(){

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }


        Integer count = 0;
        Integer seqN = 0;
        long longitude = 83071600;//8.30716
        long latitude = 469913000;//46.9913
        int ix = 10;
        int sourceIx = 20;



        //12;1;1415017784;35;<getNewEvents;86,1415017726,1415017772,11,28,4>
        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));
        //send hello message

        sendHelloMessage(cli, 0, 60,"NO");


        //send alarms
        while (count++ < 3){

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }

            latitude = latitude + 1000000;
            longitude = longitude + 100000;

            String frame = String.format("4;6;%d;%d;0;%d;%d;1;;;",
                    DateTimeUtils.getCurrentTimeStampSeconds(), seqN++, latitude, longitude);
            cli.write(frame);


        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        cli.close();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

    }


    public static void test001HibernateToObjects(){

        LOG.debug(">>test001");

        long startTime = System.currentTimeMillis();

        List<Organization> myOrgList= WebManager.getInstance().loadListFromDatabase(Organization.class);

        for(Organization o : myOrgList){
            System.out.println(String.format("Load Organization %s with %d Configurations and %d users",
                    o.getName(), o.getConfigurationMap().size(), o.getUserMap().size()));

            for(Configuration cnf : o.getConfigurationMap().values()){
                System.out.println(String.format("Configuration %s as %d Fleets", cnf.getProjectCode(),
                        cnf.getFleetMap().size()));

                for(Fleet fl : cnf.getFleetMap().values()){
                    System.out.println(String.format("Fleet %s as %d Vehicles", fl.getName(),
                            fl.getVehicleMap().size()));
                }
            }

        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        System.out.println(String.format("It took %d ms to fetch all fleet data", duration));

    }

    public void test002SendDataToQueue(){

        LOG.debug(">>test002");

        int count = 0;
        BlockingQueue<String> buffer = DataCollectorBuffer.getInstance().getBuffer();

        while(count++ < 10){

            try {
                String msg = String.format("Send %s@%s", count, count);
                System.out.println(msg);
                buffer.put(msg);
            } catch (InterruptedException e) {

                System.out.println(e.getMessage());
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }


        }

    }

    public void test003FleetCRUD(){

        LOG.debug(">>test003");
        //load fleets
        long startTime = System.currentTimeMillis();

        List<Fleet> myFleetList= WebManager.getInstance().loadListFromDatabase(Fleet.class);

        System.out.println(String.format("Load %d fleets ", myFleetList.size()));

        DataFactory df = new DataFactory();
        //Create Fleet
        Fleet f = new Fleet();
        f.setFleetId(300);
        String fName = df.getRandomChars(4, 16);
        f.setName(fName);
        f.setEnabled(false);
        f.create();
        myFleetList.add(f);

        //Read Fleet
        Fleet g = Fleet.read(Fleet.class, "name", fName);
        //Fleet g = Fleet.read(Fleet.class, f.getFleetId());
        assertNotNull(g);
        assertEquals(g.getName(), fName);

        //Update Fleet
        String newName = Utils.randString();
        g.setName(newName);
        assertTrue(g.update());

        //read again
        g = Fleet.read(Fleet.class, "name", newName);
        assertNotNull(g);

        assertEquals(g.getName(), newName);

        //Delete Fleet
        g.delete();
        g = null;
        assertNull(Fleet.read(Fleet.class,"name", newName));

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        System.out.println(String.format("It took %d ms to fetch all fleet data", duration));

    }

    public void test004CreateUser(){

        LOG.debug(">>test004");

        Organization organization =  Organization.read( Organization.class, "name", "RTS");
        DataFactory df = new DataFactory();
        User u;
        if(organization== null) {

            organization = new Organization();
            organization.setName("RTS");
            organization.setEmail("ondiagnostics@railtec-systems.ch");
            organization.create();
        }

        Role role = Role.read(Role.class, "name", "user");

        if(role == null){
            role = new Role();
            role.setName("user");
            role.create();

            role = Role.read(Role.class, "name", "user");

            Permission perm = Permission.read(Permission.class, "roleId", role.getRoleId());

            if(perm == null){
                perm = new Permission();
                perm.setName("grant_user_all");
                perm.setObject("main");
                perm.setPermission("all");
                perm.create();
            }

            role = Role.read(Role.class, "name", "user");

        }


        String login = df.getName();
        String email = df.getEmailAddress();

        u = User.read(User.class, "login", login);

        if(u == null){
            u = new User();
            u.setLogin(login);
            u.setEmail(email);
            u.create();
            u = User.read(User.class, "login", login);
        }

        //u.setLogin(login);
        u.setMyOrganization(organization);
        u.setMyRole(role);
        u.setRoleId(role.getRoleId());

        try {

            u = User.read(User.class, "login", login);
            assertNotNull(u);
            String pass = df.getRandomChars(8);
            u.setPassword(pass);
            u.update();
            assertTrue(u.authenticate(pass));
        } catch (NullPointerException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            if(u!=null){
                u.delete();
            }

        }



    }

    public void test006UserAuthenticateByDB(){

        LOG.debug(">>test006");
        String login = "user";
        User u = User.read(User.class, "login", login);
        assertNotNull(u);
        WebManagerFacade.getInstance().setPassword(u, "123admin");
        u = User.read(User.class, "login", login);
        try {

            assertTrue(u.authenticate("123admin"));
        } catch (NullPointerException | UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    public void test005LoadTranslations(){

        LOG.debug(">>test005");

        LanguageManager.getInstance().loadAllTranslations(TestMainSuite.class);

    }

    public void test007Translate(){
            LOG.debug(">>test007");
            String textId = "PLEASE_LOGIN_TEXT";
            String t = LanguageManager.getInstance().getValue("en", textId);
            System.out.println(t);
            assertFalse(textId.contains(t));
    }

    public void test008Translate(){

        LOG.debug(">>test008");

        String order = "";

        for(Language l : LanguageManager.getInstance().getActiveLanguages()){
            order = order.concat(l.getName());
            order = order.concat(",");
        }

        System.out.println("Ordered active languages " + order);
    }

    public void test009GetDataFromODSFile(){
        LOG.debug(">>test009");
        WebManager.getInstance().init(this.getClass());
        File file = new File("config/newbase.ods");
        WebManagerFacade.getInstance().saveEventConfigurationToDataBase(file);

        WebManager.getInstance().close();

    }

    public void test010GetProcessFromODSFile(){
        LOG.debug(">>test010");
        WebManager.getInstance().init(this.getClass());
        File file = new File("config/pdbase.ods");
        WebManagerFacade.getInstance().saveProcessConfigurationToDataBase(file, 1);

        WebManager.getInstance().close();

    }

    public void test011SaveGenericSnapshotValues(){

        LOG.debug(">>test011");
        WebManager.getInstance().init(this.getClass());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        if(WebManager.getInstance().isStarted()){
            WebManager.getInstance().close();

        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        WebManager.getInstance().init(this.getClass());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        Map myRestrictions = new HashMap();
        Vehicle v = WebManagerFacade.getInstance().getVehicle(globalVid);
        DataScanCollector dataScanCollector = DataScanCollector.read(DataScanCollector.class, "fleetId", v.getFleetId());

        if(dataScanCollector == null){
            //create a Scan Configuration
            dataScanCollector = new DataScanCollector();
            dataScanCollector.setPullTime(3);
            dataScanCollector.setFleetId(v.getFleetId());
            dataScanCollector.create();
            //read back
            dataScanCollector = DataScanCollector.read(DataScanCollector.class, "fleetId", v.getFleetId());

            //update some tags with data scan

            myRestrictions.put("type", 3);
            List tagList = DataTag.getList(DataTag.class, myRestrictions);

            for(Object o: tagList){
                if(o instanceof  DataTag){
                    ((DataTag) o).setDataScanCollectorId(dataScanCollector.getDataScanCollectorId());
                    ((DataTag) o).update();
                }
            }
        }

        int count = 0;

        sendLifeSigns();


        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        //in the end print the values on the table
        myRestrictions.clear();
        myRestrictions.put("vehicleId", globalVid);
        List dataValues = SnapShotGenericValue.getList(SnapShotGenericValue.class, myRestrictions);

        for(Object o : dataValues){
            if(o instanceof SnapShotGenericValue){
                int tagId = ((SnapShotGenericValue) o).getTagId();
                DataTag t = DataTag.read(DataTag.class, tagId);

                LOG.debug(String.format("%s - Last value for %s is %d",
                        ((SnapShotGenericValue) o).getTimeStringStampFromSeconds(), t.getName(), ((SnapShotGenericValue) o).getValue()));

            }
        }

        WebManager.getInstance().close();
    }



    public void test012ConnectWithoutHello() {

        LOG.debug(">>test012");

        WebManager.getInstance().init(this.getClass());
        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));
        try {
            Thread.sleep(DiagnosticsConfig.DEFAULT_CLOSE_TIMEOUT*1000 + 5000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        assertNull(cli.readLine());

        WebManager.getInstance().close();
    }

    public void test013SaveSystemTagDataValues(){
        LOG.debug(">>test013");
        WebManager.getInstance().init(this.getClass());

        Integer seqN = 0;
        long latitude = 83071600;//8.30716
        long longitude = 469913000;//46.9913
        Integer count = 0;
        int unixTs=0;

        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));

        sendHelloMessage(cli, 0, 60,"NO");

        while (count++ < 15){

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                cli.close();
                break;
            }



            seqN++;
            latitude = latitude + 1000000;
            longitude = longitude + 100000;

            unixTs = DateTimeUtils.getCurrentTimeStampSeconds();
            //send LIFE SIGN Message 4 Times
            String frame = String.format("4;6;%d;%d;0;%d;%d;1;;;",
                    unixTs, seqN, latitude, longitude);
            cli.write(frame);


        }


        cli.close();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        //get snapshotValues for GPS coordinates
        Vehicle v =  WebManagerFacade.getInstance().getVehicle(globalVid);
        GenericTagValue value = v.getSnapShotValue(GenericTagValue.class,
                EventTypeEnum.TAG_DATA_TYPE_SYSTEM, DCLifeSignParamEnum.LATITUDE.name());

        assertNotNull(value);
        LOG.debug(String.format("Latitude : TimeStamp=%s RawValue=%d RealValue=%f",
                value.getTimeStringStampFromSeconds(),
                value.getValue(),
                value.getValue() * value.getScale()));
        assertTrue(value.getTimeStamp() == unixTs);
        assertTrue(value.getValue().equals(latitude));
        value = v.getSnapShotValue(GenericTagValue.class,
                EventTypeEnum.TAG_DATA_TYPE_SYSTEM, DCLifeSignParamEnum.LONGITUDE.name());

        LOG.debug(String.format("Longitude: TimeStamp=%s RawValue=%d RealValue=%f",
                value.getTimeStringStampFromSeconds(),
                value.getValue(),
                value.getValue() * value.getScale()));
        assertTrue(value.getTimeStamp() == unixTs);
        assertTrue(value.getValue().equals(longitude));

        WebManager.getInstance().close();

    }

    public void test014ConnectWithoutLifeSign() {

        LOG.debug(">>test014");
        WebManager.getInstance().init(this.getClass());
        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));
        int timeOut = 1;
        //send hello message
        sendHelloMessage(cli, 0, timeOut, "NO");
        assertTrue(cli.isConnected());
        try {
            Thread.sleep(timeOut * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        //send ping
        cli.write("4;6;0;0;0;0;0;0;;;");
        assertTrue(cli.isConnected());
        try {
            Thread.sleep((timeOut + DiagnosticsConfig.EXTRA_TIMEOUT_VALUE/2) *1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        assertTrue(cli.isConnected());
        try {
            Thread.sleep((timeOut + DiagnosticsConfig.EXTRA_TIMEOUT_VALUE) * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        assertFalse(cli.isConnected());

        WebManager.getInstance().close();

    }

    public void test015SyncAlarmDataBase(){

        LOG.debug(">>test015");

        WebManager.getInstance().init(this.getClass());

        //print current alarms
        Vehicle v = WebManagerFacade.getInstance().getVehicle(globalVid);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        Integer count = 0;
        Integer seqN = 0;
        long longitude = 83071600;//8.30716
        long latitude = 469913000;//46.9913
        int ix = 10;
        int sourceIx = 20;



            //12;1;1415017784;35;<getNewEvents;86,1415017726,1415017772,11,28,4>
            TCPClient cli = new TCPClient();
            assertTrue(cli.init(51313));

            //send hello message

            sendHelloMessage(cli, seqN++, 60,"NO");


            //send LIFE SIGN Message
            String frame = String.format("4;6;%d;%d;0;%d;%d;1;;;",
                     DateTimeUtils.getCurrentTimeStampSeconds(), seqN, latitude, longitude);
            cli.write(frame);

            //send alarms
            while (count++ < 3){


                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    cli.close();
                    break;
                }

                //send alarm data
                //7;1;1415718580;9;4:27;;
                seqN++;
                int timeNow = DateTimeUtils.getCurrentTimeStampSeconds();
                //ev_index,ON_time_sec, ON_time_ms ,OFF_time_sec, OFF_time_ms,active_ev_count,ev_code,status_info
                frame = String.format("12;1;%d;%d;<getNewEvents;%d,%d,0,0,0,11,%d,4>", timeNow,
                        seqN, ix++,
                        timeNow-10,  sourceIx++);
                cli.write(frame);

            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }

            cli.close();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }

        System.out.println(">> wait for sync");
        //wait until is in sync
        count = 0;
        while(v.getSyncStatus() != VehicleSyncStatusEnum.SYNC_STATUS_DB_TO_VEHICLE_OK && count++ < 10){

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
        }
        System.out.println("<< wait for sync");

        System.out.println(String.format("%s have %d active alarms Sync Status=%d", globalVid, v.getActiveAlarms().size(), count));

        assertTrue( v.getActiveAlarms().size() == 3);

    }



    public void test016SyncNoMoreDataAlarmDataBase(){

        LOG.debug(">>test016");
        WebManager.getInstance().init(this.getClass());

        //print current alarms
        Vehicle v = WebManagerFacade.getInstance().getVehicle(globalVid);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }


        Integer count = 0;
        Integer seqN = 0;
        long longitude = 83071600;//8.30716
        long latitude = 469913000;//46.9913
        int ix = 10;
        int sourceIx = 20;


        //12;1;1415017784;35;<getNewEvents;86,1415017726,1415017772,11,28,4>
        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));

        //send hello message

        sendHelloMessage(cli, seqN++, 60,"NO");

        seqN++;
        int ts = DateTimeUtils.getCurrentTimeStampSeconds();

        //send LIFE SIGN Message
        String frame = String.format("4;6;%d;%d;0;%d;%d;1;;;",
                ts, seqN, latitude, longitude);
        cli.write(frame);

        //send alarms
        while (count++ < 3){


            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                cli.close();
                break;
            }

            //send alarm data
            //7;1;1415718580;9;4:27;;
            seqN++;
            ts = DateTimeUtils.getCurrentTimeStampSeconds();

            frame = String.format("12;1;%d;%d;<getNewEvents;%d,%d,0, 0, 0,11,%d,4>", ts,
                    seqN, ix++,
                    ts-10,  sourceIx++);
            cli.write(frame);

        }

        cli.write("12;1;1415013185;196;<getNewEvents;ENOACTIVEEVENTS>;");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        cli.close();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        System.out.println(">> wait for sync");
        //wait until is in sync
        count = 0;
        while(v.getSyncStatus() != VehicleSyncStatusEnum.SYNC_STATUS_ALL_SYNC && count++ < 10){

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
        }
        System.out.println("<< wait for sync");

        System.out.println(String.format("%s have %d active alarms %d", globalVid, v.getActiveAlarms().size(), count));

        assertTrue( v.getActiveAlarms().size() == 3);

    }

    public void test017SendProcessData(){

        LOG.debug(">>test017");
        WebManager.getInstance().init(this.getClass());

        //print current alarms
        Vehicle v = WebManagerFacade.getInstance().getVehicle(globalVid);

        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));
        Integer seqN = 0;

        //send hello message

        sendHelloMessage(cli, seqN++, 60,"NO");



        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            cli.close();
            return;
        }


        String frame = String.format("7;1;%d;%d;4:27,3:10,2:20;", DateTimeUtils.getCurrentTimeStampSeconds(), seqN++);
        cli.write(frame);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            cli.close();
            return;
        }

        cli.close();

        DataTag dataTag = WebManagerFacade.getInstance().getTagBySourceId(EventTypeEnum.TAG_DATA_TYPE_PD, v.getConfigurationId(), 4);
        assertNotNull(dataTag);
        GenericTagValue tv = v.getSnapShotValue(GenericTagValue.class, EventTypeEnum.TAG_DATA_TYPE_PD, dataTag);

        assertNotNull(tv);
        System.out.println(String.format("value for %s is %d", dataTag.getName(), tv.getValue()));
        assertTrue(tv.getValue() == 27);


    }

    public void test018GetTagDescriptions() {
        LOG.debug(">>test018");
        WebManager.getInstance().init(this.getClass());
        Vehicle v = WebManagerFacade.getInstance().getVehicle(globalVid);
        DataTag t = WebManagerFacade.getInstance().getTagBySourceId(EventTypeEnum.TAG_DATA_TYPE_EVENT, v.getConfigurationId(), 1);

        AlarmTagDescription alarmTagDescription = WebManagerFacade.getInstance()
                .getTagAlarmDescription("en", t.getTagId());

        assertNotNull(alarmTagDescription);

        System.out.println(alarmTagDescription.getLongDescription());

        alarmTagDescription = AlarmTagDescription.getInstance(t.getTagId(), "de");
        alarmTagDescription.setLanguage("de");
        //alarmTagDescription.setTagId(1);
        alarmTagDescription.setShortDescription("update 1");
        alarmTagDescription.setLongDescription("update 1");
        alarmTagDescription.setWorkshopDescription("update 1");
        alarmTagDescription.update();
    }

    public void test019SendGetEnvData(){
        LOG.debug(">>test019");

        WebManager.getInstance().init(this.getClass());
        //print current alarms
        //Vehicle v = WebManagerFacade.getInstance().getVehicle(vid);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        Integer count = 0;
        Integer seqN = 0;
        long longitude = 83071600;//8.30716
        long latitude = 469913000;//46.9913
        int ix = 10;
        int sourceIx = 128;
        Timestamp ts;

        //12;1;1415017784;35;<getNewEvents;86,1415017726,1415017772,11,28,4>
        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));

        //send hello message
        sendHelloMessage(cli, seqN++,60,"NO");

        //get ack from hello
        System.out.println(cli.readLine());

        seqN++;
        ts = DateTimeUtils.getCurrentTimeStamp();

        //send LIFE SIGN Message
        String frame = String.format("4;6;%d;%d;0;%d;%d;0;;;",
                ts.getTime()/1000, seqN, latitude, longitude);
        cli.write(frame);
        //get ack from LS
        System.out.println(cli.readLine());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        //<getNewEvents;1,1455705221,486,1455707246,82,1,128,4>
        //send alarm data
        //7;1;1415718580;9;4:27;;
        seqN++;
        ts = DateTimeUtils.getCurrentTimeStamp();
        int timeNow = (int)(ts.getTime()/1000);
        //ev_index,ON_time_sec, ON_time_ms ,OFF_time_sec, OFF_time_ms,active_ev_count,ev_code,status_info
        frame = String.format("12;1;%d;%d;<getNewEvents;%d,%d,0,%d,0,11,%d,4>", timeNow,seqN,
                ix, timeNow-10,timeNow-5,  sourceIx);
        cli.write(frame);
        System.out.println(cli.readLine());

        while (count++<3) {

            frame = cli.readLine();
            //get ack from events
            System.out.println(frame);

            if(frame == null){
                break;
            }

            if (frame.contains("getEnvData")) {
                break;
            }
        }


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }



        cli.close();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

    }

    public void test020HandleEnvData(){

        LOG.debug(">>test020");
        WebManager.getInstance().init(this.getClass());

        if(WebManager.getInstance().isStarted()){
            WebManager.getInstance().close();
        }
        WebManager.getInstance().init(this.getClass());
        //12;1;1415017784;35;<getNewEvents;86,1415017726,1415017772,11,28,4>
        Integer count = 0;
        Integer seqN = 0;
        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }


        sendHelloMessage(cli, seqN++, 60, "NO");

        long longitude = 83071600;//8.30716
        long latitude = 469913000;//46.9913
        int ix = 10;
        int sourceIx = 1;
        Long timeNow = DateTimeUtils.getCurrentTimeStamp().getTime();
        timeNow = timeNow - 5000;
        Integer endTS = (int)(timeNow / 1000);
        Integer endTSms = (int)(timeNow % 1000);
        timeNow = timeNow - 5000;
        Integer startTS = (int)(timeNow / 1000);
        Integer starTSms = (int)(timeNow % 1000);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        //send alarm data
        //7;1;1415718580;9;4:27;;
        //ev_index,ON_time_sec, ON_time_ms ,OFF_time_sec, OFF_time_ms,active_ev_count,ev_code,status_info
        String frame = String.format("12;1;%d;%d;<getNewEvents;%d,%d,%d,%d,%d,11,%d,4>",
                timeNow/1000, seqN++, ix,
                startTS, starTSms, endTS, endTSms,
                sourceIx);
        cli.write(frame);
        LOG.debug(cli.readLine());

        //send LS
        while (count++ < 60){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }


            frame = cli.readLine();
            //System.out.println(">>>>> CLI_TEST: " + frame);
            if(frame == null){
                break;
            }

            //if ask for get new event
            //reply something
            if (frame.contains("getEnvData;10;0")) {

                if(count < 10) {

                    //reply with data
                    frame = String.format("12;1;%d;%d;<getEnvData;10;0;DATA;%d;%d;00000000000000000000000000000000000000000000000000000000000000>",
                            DateTimeUtils.getCurrentTimeStampSeconds(), seqN, (int)(timeNow/1000), (int)(timeNow % 1000));
                    cli.write(frame);
                    timeNow = timeNow + 1000;
                    System.out.println("<<<<< CLI_TEST:" + frame);
                } else {

                    frame = String.format("12;1;%d;%d;<getEnvData;10;0;END;>",
                            DateTimeUtils.getCurrentTimeStampSeconds(), seqN);
                    cli.write(frame);
                    break;

                }

            }

            if (frame.contains("getEnvData;10;1")) {

                if(count < 10) {

                    //reply with data
                    frame = String.format("12;1;%d;%d;<getEnvData;10;1;DATA;%d;%d;00000000000000000000000000000000000000000000000000000000000000>",
                            DateTimeUtils.getCurrentTimeStampSeconds(), seqN, (int)(timeNow/1000), (int)(timeNow % 1000));
                    cli.write(frame);
                    timeNow = timeNow + 1000;
                    //System.out.println("<<<<< CLI_TEST:" + frame);
                } else {

                    frame = String.format("12;1;%d;%d;<getEnvData;10;1;END;>",
                            DateTimeUtils.getCurrentTimeStampSeconds(), seqN);
                    cli.write(frame);
                    break;

                }

            }
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }


        cli.close();
    }



    public void test021GetEnvData(){

        LOG.debug(">>test021");
        if(WebManager.getInstance().isStarted()){
            WebManager.getInstance().close();
        }
        WebManager.getInstance().init(this.getClass());
        List myDataList  = WebManagerFacade.getInstance().getAlarmEnvironmentData(globalVid, 25);
        assertTrue(myDataList.size() > 0);

    }




    public void test022HandleEmptyEnvData(){

        LOG.debug(">>test022");
        WebManager.getInstance().init(this.getClass());

        Integer count = 0;
        Integer seqN = 0;
        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        //send hello message
        sendHelloMessage(cli, seqN++,60,"NO");


        int ix = 10;

        Long timeNow = DateTimeUtils.getCurrentTimeStamp().getTime();
        timeNow = timeNow - 5000;
        Integer endTS = (int)(timeNow / 1000);
        Integer endTSms = (int)(timeNow % 1000);
        timeNow = timeNow - 5000;
        Integer startTS = (int)(timeNow / 1000);
        Integer starTSms = (int)(timeNow % 1000);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        //send alarm data
        //7;1;1415718580;9;4:27;;
        //ev_index,ON_time_sec, ON_time_ms ,OFF_time_sec, OFF_time_ms,active_ev_count,ev_code,status_info
        String frame = String.format("12;1;%d;%d;",
                timeNow/1000, seqN++, ix);
        cli.write(frame);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        cli.close();
    }



    public void test023ReloadDataScanner(){

        LOG.debug(">>test023");
        WebManager.getInstance().init(this.getClass());

        Vehicle v = WebManagerFacade.getInstance().getVehicle(globalVid);
        DataScanCollector dataScanCollector = DataScanCollector.read(DataScanCollector.class, "fleetId", v.getFleetId());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        dataScanCollector.setPullTime(dataScanCollector.getPullTime() + 10);
        dataScanCollector.update();

        WebManager.getInstance().reloadDataScanner();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        dataScanCollector.setPullTime(dataScanCollector.getPullTime() - 10);
        dataScanCollector.update();

    }

    public void test024SaveXml(){

        LOG.debug(">>test0024");
        WebManager.getInstance().init(this.getClass());
        List myList = WebManagerFacade.getInstance().getAllFleets();
        WebManagerFacade.getInstance().saveListToXml(Fleet.class, myList);

    }

    public void test025ImportData(){
        LOG.debug(">>test025");
        WebManager.getInstance().init(this.getClass());
        File f = new File("config/20150130.zip");

        Vehicle v = WebManagerFacade.getInstance().getVehicle(globalVid);
        WebManagerFacade.getInstance().importAlarms(v, null, f);

    }

    public void test026SetFavourite(){
        LOG.debug(">>test026");
        WebManager.getInstance().init(this.getClass());
        User user = WebManagerFacade.getInstance().getUser("user");
        Vehicle vehicle = WebManagerFacade.getInstance().getVehicle(globalVid);

        boolean isFavourite = user.isFavourite(vehicle.getVehicleId());
        LOG.debug(String.format("Favourite =  %s", isFavourite));
        isFavourite = !isFavourite;
        user.setFavourite(vehicle.getVehicleId(), isFavourite);
        boolean newIsFavourite = user.isFavourite(vehicle.getVehicleId());

        assertEquals(isFavourite, newIsFavourite);



    }

    public void test027WaitForConfigurePDAndStartStopPD(){
        LOG.debug(">>test027");
        WebManager.getInstance().init(this.getClass());
        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));
        int timeOut = 30;
        int seqN = 0;
        //send hello message
        sendHelloMessage(cli, seqN++,timeOut,"NO");

        assertTrue(cli.isConnected());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }

        String rcv;
        Boolean found = false;
        int count = 0;
        while((rcv = cli.readLine())!=null && count++< 10){

            LOG.debug("#### RX:" + rcv);
            //15;2;<Time Stamp>;<Sequence Number>;<Offline Process Data>;<Online Process Data>;
            if(rcv.contains("15;2;")){
                found = true;
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }

        }

        assertTrue(found);

        Vehicle v = WebManagerFacade.getInstance().getVehicle(globalVid);
        v.incrementSessionCount();

        found = false;
        count = 0;
        while((rcv = cli.readLine())!=null && count++< 10){

            LOG.debug("#### RX:" + rcv);
            //5;1;<Time Stamp>;<Sequence Number>;<Update Interval>;
            if(rcv.contains("5;1;")){
                found = true;
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }

        }
        assertTrue(found);

        v.decrementSessionCount();

        found = false;
        count = 0;
        while((rcv = cli.readLine())!=null && count++< 10){

            LOG.debug("#### RX:" + rcv);
            //6;1;<Time Stamp>;<Sequence Number>;<Cause ID>;
            if(rcv.contains("6;1;")){
                found = true;
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }

        }
        assertTrue(found);




    }

    public void test028RestartDiagD(){

        LOG.debug(">>test028");
        WebManager.getInstance().init(this.getClass());
        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));
        int timeOut = 30;
        int seqN = 0;
        //send hello message

        sendHelloMessage(cli, seqN++, timeOut,"YES");
        assertTrue(cli.isConnected());

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }


        cli.close();



    }


    public void test029SendLSEmptyData(){
        LOG.debug(">>test029");
        WebManager.getInstance().init(this.getClass());
        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));
        int timeOut = 30;
        //send hello message
        sendHelloMessage(cli, 0, timeOut,"NO");

        assertTrue(cli.isConnected());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        int seqN = 0;
        int count = 0;
        while(count++ < 3){
            //send LIFE SIGN Message 4 Times
            String frame = String.format("4;6;%d;%d;0;;;0;;;",
                    DateTimeUtils.getCurrentTimeStampSeconds(), ++seqN);
            cli.write(frame);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                cli.close();
                break;
            }

        }




        cli.close();



    }

    public void test030ChangePassword(){

        LOG.debug(">>test030");
        WebManager.getInstance().init(this.getClass());
        User u = User.read(User.class, "login", "admin");

        ReturnCode ret = WebManagerFacade.getInstance().changeUserPassword(u, "admin123", "123admin", "123admin");

        LOG.debug(String.format("Change password result is %s", ret.toString()));

    }

    public void test031SendLSNegativeData(){

        LOG.debug(">>test031");

        WebManager.getInstance().init(this.getClass());
        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));
        int timeOut = 30;
        //send hello message
        sendHelloMessage(cli, 0, timeOut, "NO");

        assertTrue(cli.isConnected());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        int seqN = 0;
        int count = 0;
        while(count++ < 3){
            //send LIFE SIGN Message 4 Times
            //<header><train status><lat><long><gps status><gsm status><offline data>
            //4;3;1439283157;292;0;469923290;83111715;3;0;0:-18,1:116,2:159,;
            String frame = String.format("4;6;%d;%d;0;469923290;83111715;2;0;0:-18,1:116,2:159,;",
                    DateTimeUtils.getCurrentTimeStampSeconds(), ++seqN);
            cli.write(frame);

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                cli.close();
                break;
            }

        }

        cli.close();



    }

    public void test032SendLSSavePosition(){

        LOG.debug(">>test032");

        WebManager.getInstance().init(this.getClass());
        TCPClient cli = new TCPClient();
        assertTrue(cli.init(51313));
        int timeOut = 30;
        //send hello message
        sendHelloMessage(cli, 0, timeOut,"NO");

        assertTrue(cli.isConnected());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();

        }
        int seqN = 0;
        int count = 0;

            String frame = String.format("4;6;%d;%d;0;469923290;83111715;2;0;;",
                    DateTimeUtils.getCurrentTimeStampSeconds(), ++seqN);
            cli.write(frame);



        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            cli.close();

        }

        cli.close();



    }


  //DataCollectorParser:166 - Received NEW ALARM 13,1426512756,536,1426512766,636,2,202,4>
  //no DataTag found for reference 202 please review diagd settings


}
