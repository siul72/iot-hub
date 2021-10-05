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

package co.luism.lukisoftiot.common;

import co.luism.lukisoftiot.enterprise.*;
import co.luism.lukisoftiot.utils.HibernateUtil;
import co.luism.lukisoftiot.webapputils.LanguageManager;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luis on 05.09.14.
 */
public final class Utils {

    Utils(){

    }

    private static final Logger LOG = Logger.getLogger(Utils.class);
    private static final SecureRandom rand = new SecureRandom();

    private static final String CREATE_TABLE_ORG_SQL = "CREATE TABLE Organization (" +
            "organizationId INT(32) UNSIGNED NOT NULL AUTO_INCREMENT," +
            "name  VARCHAR(32) NOT NULL," +
            "addressStreet VARCHAR(32) NULL," +
            "addressPostCode VARCHAR(32) NULL," +
            "email VARCHAR(32) NOT NULL," +
            "updateBy VARCHAR(32) NOT NULL DEFAULT ''," +
            "updateTime TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "enabled BOOL NOT NULL DEFAULT TRUE ," +
            "PRIMARY KEY (organizationId)," +
            "UNIQUE (email)," +
            "UNIQUE (name)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

    private static final String CREATE_TABLE_FLEET_SQL = "CREATE TABLE Fleet ( " +
            "fleetId INT(32) UNSIGNED NOT NULL AUTO_INCREMENT," +
            "name  VARCHAR(32) NOT NULL," +
            "enabled BOOL NOT NULL default TRUE ," +
            "updateBy VARCHAR(32) NOT NULL DEFAULT ''," +
            "updateTime TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "organizationId INT(32) UNSIGNED NOT NULL DEFAULT 0," +
            "PRIMARY KEY (fleetId)," +
            "INDEX FK_FleetOrg (organizationId)," +
            "CONSTRAINT UC_NAME UNIQUE (name,organizationId)," +
            "CONSTRAINT FK_FleetOrg FOREIGN KEY (organizationId) REFERENCES Organization (organizationId)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";



    private static final String CREATE_TABLE_ROLE_SQL ="CREATE TABLE Role (" +
            "roleId INT(32) UNSIGNED NOT NULL AUTO_INCREMENT," +
            "name  VARCHAR(32) NOT NULL," +
            "enabled BOOL NOT NULL default TRUE ," +
            "updateBy VARCHAR(32) NOT NULL DEFAULT ''," +
            "updateTime TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "PRIMARY KEY (`roleId`), UNIQUE(name)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

    private static final String CREATE_TABLE_PERMISSION_SQL ="CREATE TABLE Permission (" +
            "permissionId INT(32) UNSIGNED NOT NULL AUTO_INCREMENT," +
            "name  VARCHAR(32) NOT NULL," +
            "object  VARCHAR(32) NOT NULL," +
            "permission  VARCHAR(32) NOT NULL," +
            "updateBy VARCHAR(32) NOT NULL DEFAULT ''," +
            "updateTime TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "roleId INT(32) UNSIGNED NULL DEFAULT NULL," +
            "PRIMARY KEY (permissionId), UNIQUE(name)," +
            "INDEX FK_PermRole (roleId)," +
            "CONSTRAINT FK_PermRole FOREIGN KEY (roleId) REFERENCES Role (roleId)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

    private static final String CREATE_TABLE_USER_SQL ="CREATE TABLE User ( " +
            "userId INT(32) UNSIGNED NOT NULL AUTO_INCREMENT," +
            "login     VARCHAR(32)      NOT NULL," +
            "firstName VARCHAR(32)      NULL DEFAULT NULL," +
            "lastName  VARCHAR(32)      NULL DEFAULT NULL," +
            "password  VARCHAR(32)      NULL DEFAULT NULL," +
            "salt  VARCHAR(32)      NULL DEFAULT NULL, " +
            "email  VARCHAR(32) NOT NULL," +
            "language  VARCHAR(16) NOT NULL DEFAULT 'en'," +
            "updateBy VARCHAR(32) NOT NULL DEFAULT ''," +
            "updateTime TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "organizationId INT(32) UNSIGNED NULL DEFAULT NULL," +
            "roleId INT(32) UNSIGNED NULL DEFAULT NULL," +
            "PRIMARY KEY (userId), UNIQUE (login), UNIQUE (email)," +
            "INDEX FK_UserOrg (organizationId)," +
            "INDEX FK_UserRole (roleId)," +
            "CONSTRAINT FK_UserOrg FOREIGN KEY (organizationId) REFERENCES Organization (organizationId)," +
            "CONSTRAINT FK_UserRole FOREIGN KEY (roleId) REFERENCES Role (roleId)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";


    private static final String CREATE_TABLE_VEHICLE_SQL= "CREATE TABLE Vehicle ( "+
            "vehicleId VARCHAR(32) NOT NULL,"+
            "vehicleType VARCHAR(32) NOT NULL,"+
            "vehicleNumber VARCHAR (16) NOT NULL ,"+
            "smsNumber VARCHAR(20) NOT NULL,"+
            "protocolVersion VARCHAR(16) NOT NULL,"+
            "timeZone INT(5) SIGNED NOT NULL,"+
            "countryCode VARCHAR(8) NOT NULL,"+
            "daylightSavingTime BOOLEAN NOT NULL,"+
            "enabled BOOLEAN NOT NULL,"+
            "updateBy VARCHAR(32) NOT NULL DEFAULT '',"+
            "updateTime TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"+
            "fleetId INT(32) UNSIGNED NULL DEFAULT NULL,"+
            "PRIMARY KEY (vehicleId),"+
            "INDEX FK_Fleet (fleetId),"+
            "CONSTRAINT UC_NAME UNIQUE (vehicleType,vehicleNumber),"+
            "CONSTRAINT FK_Fleet FOREIGN KEY (fleetId) REFERENCES Fleet (fleetId)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";


    private static final String CREATE_TABLE_DATA_TAG_SQL =  "CREATE TABLE DataTag (" +
            "tagId INT(32) UNSIGNED NOT NULL AUTO_INCREMENT," +
            "sourceTagId INT(32) UNSIGNED NOT NULL DEFAULT 0," +
            "name VARCHAR(128) NOT NULL," +
            "process VARCHAR(32) NOT NULL," +
            "type VARCHAR(32) NOT NULL," +
            "engUnits VARCHAR(32) NOT NULL," +
            "valueType VARCHAR(32) NOT NULL," +
            "scale DOUBLE NOT NULL DEFAULT 1," +
            "incrementDeadBand VARCHAR(32) NOT NULL," +
            "decrementDeadBand VARCHAR(32) NOT NULL," +
            "enabled BOOLEAN NOT NULL," +
            "updateBy VARCHAR(32) NOT NULL DEFAULT ''," +
            "updateTime TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "PRIMARY KEY (tagId)," +
            " CONSTRAINT UC_SID_NAME UNIQUE (sourceTagId, name)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";



    private static final Map<Class, String>mapScripts;

    static {
        Map<Class, String> aMap = new HashMap<Class, String>();
        aMap.put(Organization.class, CREATE_TABLE_ORG_SQL);
        aMap.put(Fleet.class, CREATE_TABLE_FLEET_SQL);
        aMap.put(Vehicle.class, CREATE_TABLE_VEHICLE_SQL);
        aMap.put(User.class, CREATE_TABLE_USER_SQL);
        aMap.put(DataTag.class, CREATE_TABLE_DATA_TAG_SQL);
        aMap.put(Role.class, CREATE_TABLE_ROLE_SQL);
        aMap.put(Permission.class, CREATE_TABLE_PERMISSION_SQL);

        mapScripts = Collections.unmodifiableMap(aMap);
    }



    public static <T> File getResourceFile(Class clazz, String name){

        URL url = clazz.getClassLoader().getResource(DiagnosticsConfig.FILE_CONFIG_PATH);



        if(url == null){
            return null;
        }

        LOG.debug(String.format("Class %s, file %s , url %s", clazz.getName(), name, url.toString()));

        File f = null;
        try {

            URI uriName = url.toURI().resolve(name);

            if(uriName == null){
                LOG.error(String.format("file not found %s", name));
                return null;
            }

            if(uriName.getPath() == null){
                LOG.error(String.format("file not found %s", name));
                return null;
            }



            LOG.debug(String.format("uri %s", uriName.getPath()));

            f = new File(uriName);

        } catch (URISyntaxException e) {
            LOG.debug(String.format("uri URISyntaxException"));

        }

        return f;
    }



    public static String getResourcePath(Class clazz, String res, String name){

        URL url = clazz.getClassLoader().getResource(res);

        if(url == null){
            return null;
        }

        LOG.debug("The resource is:" + url.getPath());

        try {
                URI uriName = url.toURI().resolve(name);
                LOG.debug("The getResourcePath:" + uriName.getPath());
                return uriName.getPath();

        } catch (URISyntaxException e) {
           LOG.error("fail to get resource path");
           return null;
        }

    }

    public static File createResourceFile(Class clazz, String resource, String fileName) {

        URL url = clazz.getClassLoader().getResource(resource);

        if(url == null){
            LOG.error("resource file " + fileName + " not found in:" + resource);
            return null;
        }

        LOG.debug(String.format("new resource %s", url.getPath()));

        File f;
        try {
            URI uriName = url.toURI().resolve(fileName);

            f = new File(uriName);

        } catch (URISyntaxException e) {
            f = new File( url.getPath());
        }

        try {
            LOG.info("New resource file returned:" + f.getCanonicalPath());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }

        return f;
    }


    public static boolean createFolderInResources(String fName) {

        URL url = Utils.class.getClassLoader().getResource(fName);

        File theDir = null;
        try {
            if (url != null) {
                theDir = new File(url.toURI());
            }

        } catch (URISyntaxException e) {
            theDir = new File( url.getPath());
        }

        if(theDir == null){
            return false;
        }

        // if the directory does not exist, create it
        if (!theDir.exists()) {

            boolean result = false;

            try {
                theDir.mkdir();
                result = true;
            } catch (SecurityException se) {

                LOG.error("unable to create folder:" + se.getMessage());
            } finally {


                if (result) {
                    LOG.info("folder created: " + fName);
                }
            }
        } else {
            LOG.info("The folder " + theDir.getName() + " Exist");
            return true;
        }

        return false;
    }

    public static boolean dropTableAndCreateSchema(Class clazz){

        String sql = String.format("DROP TABLE IF EXISTS %s", clazz.getSimpleName());
        boolean result = HibernateUtil.getInstance().sendExecuteUpdate(sql);

        if(!result){

            return false;
        }

        return HibernateUtil.getInstance().sendExecuteUpdate(mapScripts.get(clazz));


    }

    public static boolean createTable(Class clazz) {

         return HibernateUtil.getInstance().sendExecuteUpdate(mapScripts.get(clazz));

    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        //Random rand = new Random();
        //SecureRandom rand = new SecureRandom();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min)) + min;

        return randomNum;
    }

    public static String randString() {

        //SecureRandom random = new SecureRandom();
        return new BigInteger(64, rand).toString(16);

    }

    public static void dropTable(Class clazz) {

        String sql = String.format("DROP TABLE IF EXISTS %s", clazz.getSimpleName());
        HibernateUtil.getInstance().sendExecuteUpdate(sql);

    }

    public static void dropTables(Class... clazzArray) {

        for(Class cl : clazzArray){
            String sql = String.format("DROP TABLE IF EXISTS %s", cl.getSimpleName());
            HibernateUtil.getInstance().sendExecuteUpdate(sql);
        }



    }


    public static String md5(String input) {

        String md5 = null;

        if(null == input){
            return null;
        }

        try {

            //Create MessageDigest object for MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");

            //Update input string in message digest
            digest.update(input.getBytes(), 0, input.length());

            //Converts message digest value in base 16 (hex)
            md5 = new BigInteger(1, digest.digest()).toString(16);

        } catch (NoSuchAlgorithmException e) {

            LOG.error("md5 generator error:" + e.getMessage());
        }

        return md5;
    }


    /**
     * From a password, a number of iterations and a salt,
     * returns the corresponding digest
     * @param iterationNb int The number of iterations of the algorithm
     * @param password String The password to encrypt
     * @param salt byte[] The salt
     * @return byte[] The digested password
     * @throws NoSuchAlgorithmException If the algorithm doesn't exist
     */
    public static byte[] getHash(int iterationNb, String password, byte[] salt) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        digest.update(salt);
        byte[] input = digest.digest(password.getBytes("UTF-8"));
        for (int i = 0; i < iterationNb; i++) {
            digest.reset();
            input = digest.digest(input);
        }
        return input;
    }


    public static String byteToBase64(byte[] b){

        byte[] c = Base64.encodeBase64(b);

        return new String(c);

    }

    public static byte[] base64ToByte(String b){

        return  Base64.decodeBase64(b.getBytes());

    }

    public static boolean tableDeleteContents(Class clazz) {

        String sql = String.format("SET foreign_key_checks = 0");
        HibernateUtil.getInstance().sendExecuteUpdate(sql);
        sql = String.format("TRUNCATE TABLE %s", clazz.getSimpleName());
        HibernateUtil.getInstance().sendExecuteUpdate(sql);
        sql = String.format("SET foreign_key_checks = 1");

        return HibernateUtil.getInstance().sendExecuteUpdate(sql);

    }

//    // encode data on your side using BASE64
//    byte[]   bytesEncoded = Base64.encodeBase64(str .getBytes());
//    System.out.println("ecncoded value is " + new String(bytesEncoded ));
//
//    // Decode data on other side, by processing encoded data
//    byte[] valueDecoded= Base64.decodeBase64(bytesEncoded );
//    System.out.println("Decoded value is " + new String(valueDecoded));

    public static BigDecimal parseStringToBigDecimal(String myString){

        // Create a DecimalFormat that fits your requirements
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        //symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        //String pattern = "#,##0.0#";
        String pattern = "##0.0#";
        DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
        decimalFormat.setParseBigDecimal(true);

// parseStringToBigDecimal the string
        BigDecimal bigDecimal; // = (BigDecimal) decimalFormat.parseStringToBigDecimal("10,692,467,440,017.120");


        try {
            bigDecimal =   (BigDecimal) decimalFormat.parse(myString);
        } catch (ParseException e) {
            LOG.error(e);
            return null;
        }

        return bigDecimal;
        //System.out.println(bigDecimal);

    }


    public static void createSchema(String user_name, String user_password) {


        HibernateUtil.saveCredentials(user_name, user_password);
        //Instanciate Hibernate, set user name and password, create database, grant access
        //String dataBaseName =  HibernateUtil.getInstance().getDatabase();
        //String sql = String.format("CREATE DATABASE %s;", dataBaseName);
        //HibernateUtil.getInstance().sendExecuteUpdate(sql);
        //sql = String.format("GRANT ALL PRIVILEGES ON %s.* TO '%s'@'localhost';", dataBaseName, user_name);
        //HibernateUtil.getInstance().sendExecuteUpdate(sql);

        HibernateUtil a = HibernateUtil.getInstance();
        mapScripts.forEach( (cl,script) -> {
            boolean not_ok = a.sendExecuteUpdate(script);

        } );


    }

    public static boolean testDatabase() {

        String v = LanguageManager.getInstance().getValue("LOGIN_BUTTON");
        return v.compareTo("LOGIN_BUTTON") != 0;
    }
}
