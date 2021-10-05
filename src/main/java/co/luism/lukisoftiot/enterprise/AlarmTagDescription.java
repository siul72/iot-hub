package co.luism.lukisoftiot.enterprise;

import co.luism.lukisoftiot.common.DiagnosticsPersistent;
import co.luism.lukisoftiot.utils.HibernateUtil;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luis on 17.11.14.
 */
@Entity
@Table(catalog = "lukiiot")
public class AlarmTagDescription extends DiagnosticsPersistent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private Integer tagId;
    private String language;
    private String shortDescription;
    private String longDescription;
    private String workshopDescription;
    private String updateBy ="";

    public static AlarmTagDescription getInstance(Integer tagId, String language) {

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("tagId", tagId);
        stringObjectMap.put("language", language);
        Object dbObject = HibernateUtil.getInstance().sendExecuteRead(AlarmTagDescription.class, stringObjectMap);
        if(dbObject == null){
            AlarmTagDescription alarmTagDescription = new AlarmTagDescription();
            alarmTagDescription.setTagId(tagId);
            alarmTagDescription.setLanguage(language);
            alarmTagDescription.create();
            //readback
            dbObject = HibernateUtil.getInstance().sendExecuteRead(AlarmTagDescription.class, stringObjectMap);

        }

        return (AlarmTagDescription)dbObject;

    }

    @Override
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTagId() {
        return tagId;
    }

    public void setTagId(Integer tagId) {
        this.tagId = tagId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getWorkshopDescription() {
        return workshopDescription;
    }

    public void setWorkshopDescription(String workshopDescription) {
        this.workshopDescription = workshopDescription;
    }

    public String getUpdateBy() {
        return updateBy;
    }


}
