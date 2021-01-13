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

package co.luism.diagnostics.common;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

/**
 * Created by luis on 14.08.15.
 */
public class WatchDogClient {

    private static final Logger LOG = Logger.getLogger(WatchDogClient.class);
        private Socket clientSocket;
        private BufferedReader rcv;
        private final String name;
        private boolean cnxFlagLog = false;

    public WatchDogClient(String name){
            this.name = name;
        }


        public boolean init(){
            try {
                clientSocket = new Socket("localhost", 1120);
            } catch (IOException e) {
                if(!cnxFlagLog){
                    cnxFlagLog = true;
                    LOG.warn(this.name+e.getMessage());
                }

                return false;
            }
            try {

                rcv= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch(IOException e){
                cnxFlagLog = false;
                return false;
            }
            cnxFlagLog = true;
            return true;
        }

        private boolean write(String data){

            if(clientSocket == null){
                return false;
            }

            if(!clientSocket.isConnected()){
                return false;
            }

            PrintWriter outToServer;
            try {
                outToServer = new PrintWriter(clientSocket.getOutputStream());
            } catch (IOException e) {
                if(!cnxFlagLog) {
                    LOG.warn(this.name + e.getMessage());
                }
                return false;
            }

            outToServer.println(data);

            if (outToServer.checkError()){
                if(!cnxFlagLog) {
                    LOG.warn(this.name + ":error writing data...");
                }
                return false;
            }
            outToServer.flush();
            return true;
        }

        public String readLine(){
            try {
                return rcv.readLine();
            } catch (IOException e) {
                LOG.warn(this.name+e.getMessage());
                return null;
            }
        }

        public boolean isConnected(){

            return (readLine()!=null);
        }

        public void close(){
            try {
                clientSocket.close();
            } catch (IOException e) {
                LOG.warn(this.name+e.getMessage());
            }
        }

        public boolean register(int updateTime){

            return write(String.format("REGISTER;%s;%d;", this.name, updateTime));
        }

        public boolean update(){

           return write("UPDATE;");

        }

        public boolean unregister(){

            return write("UNREGISTER;");

        }


    }

