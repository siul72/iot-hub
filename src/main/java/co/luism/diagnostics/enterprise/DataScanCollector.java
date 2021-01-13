package co.luism.diagnostics.enterprise;

import co.luism.diagnostics.common.DiagnosticsPersistent;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luis on 26.01.15.
 */
@Entity
@Table(catalog = "ondiagnose")
public class DataScanCollector extends DiagnosticsPersistent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer dataScanCollectorId = 0;
    private Integer pullTime = 0;
    private Boolean enabled = true;
    private String updateBy ="";

    @javax.persistence.OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name="dataScanCollectorId")
    @javax.persistence.MapKey(name = "tagId")
    private Map<Integer, DataTag> dataTagMap = new HashMap<>();

    @ManyToOne
    @JoinColumn(name="fleetId", insertable=false, updatable=false, nullable=false)
    private Fleet myFleet = null;
    private Integer fleetId = 0;

    @Transient
    private Integer currentCount = 0;

    @Override
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Integer getDataScanCollectorId() {
        return dataScanCollectorId;
    }

    public void setDataScanCollectorId(Integer dataScanCollectorId) {
        this.dataScanCollectorId = dataScanCollectorId;
    }

    public Integer getPullTime() {
        return pullTime;
    }

    public void setPullTime(Integer pullTime) {
        this.pullTime = pullTime;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }

    public void setCurrentCount(Integer currentCount) {
        this.currentCount = currentCount;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Fleet getMyFleet() {
        return myFleet;
    }

    public void setMyFleet(Fleet myFleet) {
        this.myFleet = myFleet;
    }

    public Integer getFleetId() {
        return fleetId;
    }

    public void setFleetId(Integer fleetId) {
        this.fleetId = fleetId;
    }

    public Map<Integer, DataTag> getDataTagMap() {
        return dataTagMap;
    }

    public void setDataTagMap(Map<Integer, DataTag> dataTagMap) {
        this.dataTagMap = dataTagMap;
    }
}
