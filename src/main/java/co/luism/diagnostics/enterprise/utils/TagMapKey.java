package co.luism.diagnostics.enterprise.utils;

/**
 * Created by luis on 21.01.15.
 */
public class TagMapKey implements Comparable{

    public final Integer configurationId;
    public final Integer sourceTagId;

    public TagMapKey(Integer configurationId, Integer sourceTagId){
        this.configurationId = configurationId;
        this.sourceTagId = sourceTagId;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof TagMapKey){
            int comp = configurationId.compareTo(((TagMapKey) o).configurationId);
            if(comp != 0){
                return comp;
            }

            return sourceTagId.compareTo(((TagMapKey) o).sourceTagId);
        }

        return 0;
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof TagMapKey){
            if(this.configurationId.equals(((TagMapKey) other).configurationId) &&
                    this.sourceTagId.equals(((TagMapKey) other).sourceTagId )){
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 7 * hash + this.configurationId.hashCode();
        hash = 7 * hash + this.sourceTagId.hashCode();
        return hash;

    }
}
