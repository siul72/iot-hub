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

import org.apache.log4j.Logger;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * datacollector
 * co.luism.lukisoftiot.common
 * Created by luis on 19.09.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */

public class XmlList<T> {

    private static final String xmlFilePath = DiagnosticsConfig.FILE_CONFIG_PATH + File.separator;
    private static final Logger LOG = Logger.getLogger(XmlList.class);

    private List<T> items;
    final Class<T> typeParameterClass;
    String elements;

    public XmlList() {

        this.items = new ArrayList<T>();
        this.typeParameterClass = null;
        this.elements = null;
    }

    public XmlList(Class<T> typeParameterClass) {

        this.items = new ArrayList<T>();
        this.typeParameterClass = typeParameterClass;
    }

    public XmlList(Class<T> typeParameterClass, List<T> list) {

        this.items = list;
        this.typeParameterClass = typeParameterClass;
        setElements(this.typeParameterClass.getName());

    }

    @XmlAnyElement(lax=true)
    public List<T> getItems() {
        return items;
    }

    @XmlAttribute
    public void setElements(String elements) {
        this.elements = elements;
    }

    public String getElements() {
        return elements;
    }

    public static <T> List<T> fromXml(Class<T> clazz) throws JAXBException
    {
        //String fileName = xmlFilePath+ clazz.getSimpleName() +"List.xml";
        String fileName = clazz.getSimpleName() +"List.xml";
        fileName = Utils.getResourcePath(clazz, xmlFilePath, fileName);

        LOG.debug("location=" + fileName);
        return fromXml(clazz, fileName);
    }


    public static <T> List<T> fromXml(Class<T> clazz, String xmlLocation) throws JAXBException {

        StreamSource xml = new StreamSource(xmlLocation);
        JAXBContext jc = JAXBContext.newInstance(XmlList.class, clazz);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        XmlList<T> wrapper = (XmlList<T>) unmarshaller.unmarshal(xml,XmlList.class).getValue();
        return wrapper.getItems();
    }

    /**
     * Wrap List in Wrapper, then leverage JAXBElement to supply root element
     * information.
     */
    public void toXml(Class clazz) throws JAXBException {

        QName qName = new QName(clazz.getSimpleName());

        JAXBContext jc = JAXBContext.newInstance(XmlList.class, this.typeParameterClass);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        JAXBElement<XmlList> jaxbElement = new JAXBElement<XmlList>(qName, clazz, this);

        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(jaxbElement, stringWriter);
        // Convert StringWriter to String
        String msg = stringWriter.toString();
        LOG.info(msg);

        String fileName = this.typeParameterClass.getSimpleName() +"List.xml";
        File f = Utils.createResourceFile(clazz, xmlFilePath , fileName);
        marshaller.marshal(jaxbElement, f);


    }


}
