package co.luism.lukisoftiot.enterprise;/*
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

import co.luism.lukisoftiot.common.DiagnosticsPersistent;

import javax.persistence.*;

/**
 * webmanager
 * co.luism.lukisoftiot.enterprise
 * Created by luis on 09.10.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */
@Entity
@Table(catalog = "lukiiot")
public class Translation extends DiagnosticsPersistent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer translationId = 0;
    @ManyToOne
    @JoinColumn(name="languageId", insertable=false, updatable=false, nullable=false)
    private Language myLanguage = null;
    @Column
    private Integer languageId = 0;
    @Column
    private String textId ="";
    @Column
    private String translation = "";
    @Column
    private String updateBy ="";




    public Integer getTranslationId() {
        return translationId;
    }

    public void setTranslationId(Integer translationId) {
        this.translationId = translationId;
    }

    public Language getMyLanguage() {
        return myLanguage;
    }

    public void setMyLanguage(Language myLanguage) {
        this.myLanguage = myLanguage;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public String getTextId() {
        return textId;
    }

    public void setTextId(String textId) {
        this.textId = textId;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    @Override
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

}
