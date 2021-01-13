package co.luism.diagnostics.enterprise;

import co.luism.ksoft.iot.utils.enterprise.DataBuffer;
import co.luism.diagnostics.common.DiagnosticsPersistent;

import javax.persistence.*;

/**
 * Created by luis on 21.11.14.
 */
@Entity
@Table(catalog = "ondiagnose")
public class AlarmBuffer extends DiagnosticsPersistent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer bufferId;
    private Integer bufferIndex;
    private Integer sampleSeconds;
    private Integer sampleMilliSeconds;
    private Integer numberOfSamples;
    private Integer configurationId = 1;
    private String updateBy = "";

    public AlarmBuffer(){

    }

    public AlarmBuffer(DataBuffer buffer, Integer configurationId) {
        this.bufferIndex = buffer.getBufferID();
        this.sampleSeconds = buffer.getIntervall_s();
        this.sampleMilliSeconds = buffer.getIntervall_ms();
        this.numberOfSamples = buffer.getSize();
        this.configurationId = configurationId;
    }

    @Override
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Integer getBufferId() {
        return bufferId;
    }

    public void setBufferId(Integer bufferId) {
        this.bufferId = bufferId;
    }

    public Integer getBufferIndex() {
        return bufferIndex;
    }

    public void setBufferIndex(Integer bufferId) {
        this.bufferIndex = bufferId;
    }

    public Integer getSampleSeconds() {
        return sampleSeconds;
    }

    public void setSampleSeconds(Integer sampleSeconds) {
        this.sampleSeconds = sampleSeconds;
    }

    public Integer getSampleMilliSeconds() {
        return sampleMilliSeconds;
    }

    public void setSampleMilliSeconds(Integer sampleMilliSeconds) {
        this.sampleMilliSeconds = sampleMilliSeconds;
    }

    public Integer getNumberOfSamples() {
        return numberOfSamples;
    }

    public void setNumberOfSamples(Integer numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
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
}
