package co.luism.diagnostics.enterprise;/*
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


import co.luism.diagnostics.common.DiagnosticsPersistent;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * datacollector
 * co.luism.diagnostics.enterprise
 * Created by luis on 23.09.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */
@XmlRootElement(name = "Organization")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(catalog = "ondiagnose")
public class Organization extends DiagnosticsPersistent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer organizationId = 0;
    @Column(nullable = false, length = 128)
    private String name;
//
//    @XmlTransient
//    @OneToMany(targetEntity = Fleet.class,fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @Fetch(FetchMode.SELECT)
//    @JoinColumn(name="organizationId")
//    private List<Fleet> fleetList = new ArrayList<>();

    @XmlTransient
    @OneToMany(targetEntity = User.class,fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name="organizationId")
    private List<User> userList = new ArrayList<User>();

    @XmlTransient
    @javax.persistence.OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name="organizationId")
    @javax.persistence.MapKey(name = "projectCode")
    private Map<String, Configuration> configurationMap = new HashMap<>();

    @XmlTransient
    @javax.persistence.OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name="organizationId")
    @javax.persistence.MapKey(name = "login")
    private Map<String, User> userMap = new HashMap<>();

    @Column(nullable = true, length = 32)
    private String addressStreet;
    @Column(nullable = true, length = 32)
    private String addressPostCode;
    @Column(nullable = false, length = 32)
    private String email;
    @Column(nullable = false, length = 1)
    private Boolean enabled = true;

    @XmlTransient
    @Column
    private String updateBy ="";

    public Organization(String orgName, String email) {
        this.name = orgName;
        this.email = email;
    }

    public Organization(){

    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressPostCode() {
        return addressPostCode;
    }

    public void setAddressPostCode(String addressPostCode) {
        this.addressPostCode = addressPostCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<Fleet> getFleetList() {
        List<Fleet> myList = new ArrayList<>();

        for(Configuration cnf : this.configurationMap.values()){
            myList.addAll(cnf.getFleetMap().values());
        }

        return myList;
    }


    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Map<String, Configuration> getConfigurationMap() {
        return configurationMap;
    }

    public void setConfigurationMap(Map<String, Configuration> configurationMap) {
        this.configurationMap = configurationMap;
    }

    public Map<String, User> getUserMap() {
        return userMap;
    }

    public void setUserMap(Map<String, User> userMap) {
        this.userMap = userMap;
    }



}
