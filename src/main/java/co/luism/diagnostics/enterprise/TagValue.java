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

import javax.persistence.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


@Entity
@Table(catalog = "ondiagnose")
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public class TagValue extends DiagnosticsPersistent {

    @Id

    private Integer id = 0;
    private int tagId;
    private String vehicleId;
    private Integer timeStamp;
    private Integer milliSeconds;
    private long value;
    private double scale = 1;
    private boolean ack = false;
    private int status = 0;
    private String updateBy ="";

    protected TagValue(){

    }

    public TagValue(String vehicleId, int tagId, Integer timeStamp, Integer milliSeconds, long v, double scale){
        this.vehicleId = vehicleId;
        this.tagId = tagId;
        this.timeStamp = timeStamp;
        this.milliSeconds = milliSeconds;
        this.value = v;
        this.scale = scale;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Integer getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Integer timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Integer getMilliSeconds() {
        return milliSeconds;
    }

    public void setMilliSeconds(Integer milliSeconds) {
        this.milliSeconds = milliSeconds;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    @Override
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public String getTimeStringStampFromSeconds(){

        long vLong = (long)this.timeStamp * 1000;
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(vLong));
        return String.format("%s UTC", date);

    }



}
