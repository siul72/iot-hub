package co.luism.lukisoftiot.enterprise;

import co.luism.lukisoftiot.common.DiagnosticsPersistent;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luis on 21.01.15.
 */
@Entity
@Table(catalog = "lukiiot")
public class Configuration extends DiagnosticsPersistent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer configurationId = 0;
    private String version = "";
    private String projectCode = "";
    private String hardware = "";
    private Boolean enabled = true;
    private String updateBy ="";

    @ManyToOne
    @JoinColumn(name="organizationId", insertable=false, updatable=false, nullable=false)
    private Organization myOrganization = null;

    private Integer organizationId = 1;

    @javax.persistence.OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name="configurationId")
    @javax.persistence.MapKey(name = "name")
    private Map<String, Fleet> fleetMap = new HashMap<>();


    @Override
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;

    }

    public Integer getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(Integer configurationId) {
        this.configurationId = configurationId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getHardware() {
        return hardware;
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public Organization getMyOrganization() {
        return myOrganization;
    }

    public void setMyOrganization(Organization myOrganization) {
        this.myOrganization = myOrganization;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public Map<String, Fleet> getFleetMap() {
        return fleetMap;
    }

    public void setFleetMap(Map<String, Fleet> fleetMap) {
        this.fleetMap = fleetMap;
    }
}
