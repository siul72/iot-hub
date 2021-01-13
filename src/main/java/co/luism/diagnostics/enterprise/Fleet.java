package co.luism.diagnostics.enterprise;

import co.luism.diagnostics.common.DiagnosticsConfig;
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
 * co.luism.datacollector.enterprise
 * Created by luis on 18.09.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */
@XmlRootElement(name = "Fleet")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity
@Table(catalog = "ondiagnose")
public class Fleet extends DiagnosticsPersistent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(unique = true, nullable = false)
    private Integer fleetId = 0;
    @Column(nullable = false, length = 32)
    private String name;
    @Column(nullable = false, length = 1)
    private Boolean enabled = true;
    @Column(nullable = false, length = 32)
    private String icon = DiagnosticsConfig.DEFAULT_FLEET_ICON;
    @Column(nullable = false, length = 32)
    private String mapPointer = DiagnosticsConfig.DEFAULT_FLEET_ICON;

    @XmlTransient
    @OneToMany(targetEntity = Vehicle.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name="fleetId")
    private List<Vehicle> vehicleList = new ArrayList<>();

    @XmlTransient
    @OneToMany(targetEntity = DataScanCollector.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name="fleetId")
    private List<DataScanCollector> DataScannerList = new ArrayList<>();


    @XmlTransient
    @OneToMany(targetEntity = Vehicle.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name="fleetId")
    @javax.persistence.MapKey(name = "vehicleId")
    private final Map<String, Vehicle> vehicleMap = new HashMap<>();

    @XmlTransient
    @ManyToOne
    @JoinColumn(name="configurationId", insertable=false, updatable=false, nullable=false)
    private Configuration myConfiguration = null;
    private Integer configurationId = 1;

    @XmlTransient
    @Column
    private String updateBy ="";

    public Fleet(String fleetName) {
        this.name = fleetName;
    }
    public Fleet(){

    }

    public Integer getFleetId() {
        return fleetId;
    }
    public void setFleetId(Integer fleetId) {
        this.fleetId = fleetId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Boolean getEnabled() {
        return enabled;
    }
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    public List<Vehicle> getVehicleList() {
        return vehicleList;
    }
    public void setVehicleList(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }
    public Configuration getMyConfiguration() {
        return myConfiguration;
    }
    public void setMyConfiguration(Configuration myConfiguration) {
        this.myConfiguration = myConfiguration;
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
    public Map<String, Vehicle> getVehicleMap() {
        return vehicleMap;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getMapPointer() {
        return mapPointer;
    }

    public void setMapPointer(String mapPointer) {
        this.mapPointer = mapPointer;
    }

    public List<DataScanCollector> getDataScannerList() {
        return DataScannerList;
    }

    public void setDataScannerList(List<DataScanCollector> dataScannerList) {
        DataScannerList = dataScannerList;
    }
}
