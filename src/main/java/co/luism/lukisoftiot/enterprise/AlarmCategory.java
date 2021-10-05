package co.luism.lukisoftiot.enterprise;

import co.luism.lukisoftiot.common.DiagnosticsPersistent;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.*;

/**
 * Created by luis on 30.10.14.
 */
@Entity
@Table(catalog = "lukiiot")
public class AlarmCategory extends DiagnosticsPersistent implements Comparable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer categoryId;
    private Integer categoryIndex;
    private Integer bufferId;
    private String name;


    @OneToMany(targetEntity = CategorySignalMap.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name="categoryId")
    @javax.persistence.MapKey(name = "signalId")
    private final Map<Integer, CategorySignalMap> webMapCategorySignalMap = new HashMap<>();

    @ManyToOne
    @JoinColumn(name="bufferId", insertable=false, updatable=false, nullable=false)
    private AlarmBuffer myBuffer = null;

    private String updateBy ="";

    public AlarmCategory(){

    }

    public AlarmCategory(Integer categoryIndex, String name, Integer bufferId) {

        this.categoryIndex = categoryIndex;
        this.name = name;
        this.bufferId = bufferId;

    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getCategoryIndex() {
        return categoryIndex;
    }

    public void setCategoryIndex(Integer categoryId) {
        this.categoryIndex = categoryId;
    }

    public Integer getBufferId() {
        return bufferId;
    }

    public void setBufferId(Integer bufferId) {
        this.bufferId = bufferId;
    }

    public AlarmBuffer getMyBuffer() {
        return myBuffer;
    }

    public void setMyBuffer(AlarmBuffer myBuffer) {
        this.myBuffer = myBuffer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, CategorySignalMap> getWebMapCategorySignalMap() {
        return webMapCategorySignalMap;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    @Override
    public void setUpdateBy(String updateBy) {
        this.updateBy=updateBy;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof AlarmCategory){

            int comp = this.bufferId.compareTo(((AlarmCategory) o).getBufferId());

            if(comp != 0){
                return comp;
            }

            return this.categoryIndex.compareTo(((AlarmCategory) o).getCategoryIndex());
        }

        return 0;
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof AlarmCategory){
            if(this.bufferId.equals(((AlarmCategory) other).getBufferId()) &&
                    this.getCategoryIndex().equals(((AlarmCategory) other).getCategoryIndex())){
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 7 * hash + this.bufferId.hashCode();
        hash = 7 * hash + this.categoryIndex.hashCode();
        return hash;

    }

    public int getSizeInBytes() {
        Integer count = 0;
        for(CategorySignalMap categorySignalMap : webMapCategorySignalMap.values()){
         count = count + categorySignalMap.getSignalSize();
        }

        double total = (double)count / 8;
        count = (int)Math.ceil(total);
        return count;

    }
}
