package co.luism.diagnostics.enterprise;
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

/**
 * webmanager
 * co.luism.diagnostics.enterprise
 * Created by luis on 09.10.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */

import co.luism.diagnostics.common.DiagnosticsPersistent;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(catalog = "ondiagnose")
public class Language extends DiagnosticsPersistent implements Comparable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer languageId = 0;
    @Column(unique = true, nullable = false)
    private String name = "";
    @Column
    private String flag ="";
    @Column(nullable = false, length = 1)
    private boolean enabled = true;

    @OneToMany(targetEntity = Translation.class,fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name="languageId")
    private List<Translation> translationList = new ArrayList<>();

    @Column
    private String updateBy ="";

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public List<Translation> getTranslationList() {
        return translationList;
    }

    public void setTranslationList(List<Translation> translationList) {
        this.translationList = translationList;
    }

    @Override
    public int compareTo(Object o) {

        if(o instanceof Language){
            Language l = (Language)  o;
            return name.compareTo(l.getName());
        }

        return 0;

    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Language){
            Language l = (Language)  o;
            if(this.getLanguageId().equals(l.getLanguageId()) && this.getName().equals(l.getName())){
                return true;
            }

        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 7 * hash + this.name.hashCode();
        hash = 7 * hash + this.languageId.hashCode();
        return hash;

    }
}

