package co.luism.lukisoftiot.common;
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

import org.apache.log4j.Logger;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringWriter;

/**
 * datacollector
 * co.luism.lukisoftiot.common
 * Created by luis on 22.09.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */


    public class XmlObject<T> {

        private static final String xmlFilePath = DiagnosticsConfig.FILE_CONFIG_PATH + File.separator;
        private static final Logger LOG = Logger.getLogger(XmlList.class);



        public static <T> T fromXml(Class<T> clazz) throws JAXBException {

            String xmlLocation = Utils.getResourcePath(clazz, xmlFilePath, clazz.getSimpleName() + ".xml");
            StreamSource xml = new StreamSource(xmlLocation);
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            T wrapper = (T) unmarshaller.unmarshal(xml);
            return wrapper;

        }

        /**
         * Wrap List in Wrapper, then leverage JAXBElement to supply root element
         * information.
         */
        public void toXml() throws JAXBException {

            JAXBContext jc = JAXBContext.newInstance(this.getClass());
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);


            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(this, stringWriter);
            // Convert StringWriter to String
            String msg = stringWriter.toString();
            LOG.info(msg);

            String fileName = xmlFilePath + this.getClass().getSimpleName() +".xml";
            File f = Utils.createResourceFile(this.getClass(), DiagnosticsConfig.FILE_CONFIG_PATH, fileName);
            marshaller.marshal(this, f);


        }

        /**
         * Wrap List in Wrapper, then leverage JAXBElement to supply root element
         * information.
         */
        public void toXml(File f) throws JAXBException {

            JAXBContext jc = JAXBContext.newInstance(this.getClass());
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);


            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(this, stringWriter);
            // Convert StringWriter to String
            String msg = stringWriter.toString();
            LOG.info(msg);

            marshaller.marshal(this, f);


        }


    }


