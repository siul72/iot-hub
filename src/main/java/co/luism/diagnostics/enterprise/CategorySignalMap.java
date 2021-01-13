package co.luism.diagnostics.enterprise;

import co.luism.diagnostics.common.DiagnosticsPersistent;

import javax.persistence.*;

/**
 * Created by luis on 30.10.14.
 */
@Entity
@Table(catalog = "ondiagnose")
public class CategorySignalMap extends DiagnosticsPersistent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private Integer categoryId;
    private Integer position;
    private Integer signalId;
    private Integer signalSize;
    private String updateBy ="";

    public CategorySignalMap(){

    }

    public CategorySignalMap(Integer categoryId, Integer signalId, Integer signalSize, Integer position) {
        this.categoryId = categoryId;
        this.signalId = signalId;
        this.signalSize = signalSize;
        this.position = position;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getSignalId() {
        return signalId;
    }

    public void setSignalId(Integer signalId) {
        this.signalId = signalId;
    }

    public Integer getSignalSize() {
        return signalSize;
    }

    public void setSignalSize(Integer signalSize) {
        this.signalSize = signalSize;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getUpdateBy() {
        return updateBy;
    }


    @Override
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

}
