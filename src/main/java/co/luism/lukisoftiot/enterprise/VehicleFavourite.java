package co.luism.lukisoftiot.enterprise;

import co.luism.lukisoftiot.common.DiagnosticsPersistent;

import javax.persistence.*;

/**
 * Created by luis on 13.02.15.
 */
@Entity
@Table(catalog = "lukiiot")
public class VehicleFavourite extends DiagnosticsPersistent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer favItemId;
    private Integer userId;
    private String vehicleId;
    private Boolean favourite;
    private String updateBy = "";

    public VehicleFavourite() {

    }

    public VehicleFavourite(Integer userId, String vehicleId, Boolean favourite) {
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.favourite = favourite;
    }

    @Override
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Integer getFavItemId() {
        return favItemId;
    }

    public void setFavItemId(Integer favItemId) {
        this.favItemId = favItemId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public Boolean getFavourite() {
        return favourite;
    }

    public void setFavourite(Boolean favourite) {
        this.favourite = favourite;
    }
}
