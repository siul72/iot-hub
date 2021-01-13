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

package co.luism.diagnostics.enterprise;


import co.luism.diagnostics.common.DiagnosticsPersistent;
import co.luism.diagnostics.common.EventTypeEnum;
import co.luism.diagnostics.common.ProcessEnum;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DataTag")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(catalog = "ondiagnose")
public class DataTag extends DiagnosticsPersistent {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer tagId = 0;
    private Integer sourceTagId = 0;
    private Integer process = ProcessEnum.PROCESS_NONE.getValue();
    private String name;
    private Integer type = EventTypeEnum.TAG_DATA_TYPE_NONE.getValue();
    private String engUnits = "";
    private String valueType = "";
    private Integer incrementDeadBand = 0;
    private Integer decrementDeadBand = 0;
    private boolean enabled = true;
    private boolean preData = false;
    private boolean postData = false;
    private double scale = 1;
    private Integer configurationId;
    private Integer dataScanCollectorId;
    private String updateBy ="";

    public DataTag(EventTypeEnum typeEnum, Integer configurationId, Integer sourceTagId, String name) {
        //this(sourceTagId, configurationId , name);
        this.type = typeEnum.getValue();
        this.configurationId = configurationId;
        this.sourceTagId = sourceTagId;
        this.name = name;
    }

//    public DataTag(Integer configurationId, Integer sourceTagId, String name) {
//        this.configurationId = configurationId;
//        this.sourceTagId = sourceTagId;
//        this.name = name;
//    }

    public DataTag(){

    }

    public Integer getTagId() {
        return tagId;
    }
    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public Integer getSourceTagId() {
        return sourceTagId;
    }

    public void setSourceTagId(Integer sourceTagId) {
        this.sourceTagId = sourceTagId;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }

    public String getName() {
        return name;
    }
    public void setName(String tagName) {
        this.name = tagName;
    }


    public String getValueType() {
        return valueType;
    }
    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getEngUnits() {
        return engUnits;
    }
    public void setEngUnits(String engUnits) {
        this.engUnits = engUnits;
    }

    public Integer getIncrementDeadBand() {
        return incrementDeadBand;
    }
    public void setIncrementDeadBand(Integer incrementDeadBand) {
        this.incrementDeadBand = incrementDeadBand;
    }

    public Integer getDecrementDeadBand() {
        return decrementDeadBand;
    }
    public void setDecrementDeadBand(Integer decrementDeadBand) {
        this.decrementDeadBand = decrementDeadBand;
    }

    public Integer getProcess() {
        return process;
    }
    public void setProcess(Integer tagProcess) {
        this.process = tagProcess;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPreData() {
        return preData;
    }

    public void setPreData(boolean preData) {
        this.preData = preData;
    }

    public boolean isPostData() {
        return postData;
    }

    public void setPostData(boolean postData) {
        this.postData = postData;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public Integer getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Integer configurationId) {
        this.configurationId = configurationId;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Integer getDataScanCollectorId() {
        return dataScanCollectorId;
    }

    public void setDataScanCollectorId(Integer dataScanCollectorId) {
        this.dataScanCollectorId = dataScanCollectorId;
    }


}
