package co.luism.lukisoftiot.common;


import co.luism.lukisoftiot.utils.HibernateUtil;
import org.apache.log4j.Logger;
import org.hibernate.TransactionException;

import java.util.List;
import java.util.Map;

/**
 * datacollector
 * co.luism.lukisoftiot.datacollector.enterprise
 * Created by luis on 18.09.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */
public abstract class DiagnosticsPersistent implements java.io.Serializable {
    private static final Logger LOG = Logger.getLogger(DiagnosticsPersistent.class);
    public abstract void setUpdateBy(String updateBy);

    public boolean create(){
        try{
            return HibernateUtil.getInstance().sendExecuteCreate(this);
        } catch (TransactionException e){
            LOG.error(e);
            return false;
        }
    }

    public static <T> T read(Class clazz, int id){
       try{
            return  HibernateUtil.getInstance().sendExecuteRead(clazz, id);
        } catch (TransactionException e){
            LOG.error(e);
            return null;
        }
    }

    public static <T> T read(Class clazz, String propertyName, Object propertyValue){
        try{
            return  HibernateUtil.getInstance().sendExecuteRead(clazz, propertyName, propertyValue);
        } catch (TransactionException e){
            LOG.error(e);
            return null;
        }

    }

    public static <T> T read(Class clazz, Map myRestriction){
        try{
            return HibernateUtil.getInstance().sendExecuteRead(clazz, myRestriction);
        } catch (TransactionException e){
            LOG.error(e);
            return null;
        }
    }

    public static String readSingleProperty(Class clazz, String propertyName, String propertyValue, String returnValue){
        try{
            return HibernateUtil.getInstance().sendExecuteReadSingleProperty(clazz,
                    propertyName, propertyValue, returnValue);
        } catch (TransactionException e){
            LOG.error(e);
            return null;
        }

    }

    public boolean update() {
        try{
            return HibernateUtil.getInstance().sendExecuteUpdate(this);
        } catch (TransactionException e){
            LOG.error(e);
            return false;
        }

    }

    public static boolean update(Class clazz, String vehicleId, List<Object> myRestrictions, long value) {
        try{
            return HibernateUtil.getInstance().sendExecuteUpdateValue(clazz, vehicleId, myRestrictions, value);
        } catch (TransactionException e){
            LOG.error(e);
            return false;
        }

    }

    public boolean delete() {
        try{
            return HibernateUtil.getInstance().sendExecuteDelete(this);
        } catch (TransactionException e){
            LOG.error(e);
            return false;
        }

    }

    public static boolean delete(Class clazz, Map myRestriction){
        try{
            return HibernateUtil.getInstance().sendExecuteDelete(clazz, myRestriction);
        } catch (TransactionException e){
            LOG.error(e);
            return false;
        }

    }

    public static <T> List<T> getList(Class clazz){
        try{
            return HibernateUtil.getInstance().getList(clazz);
        } catch (TransactionException e){
            LOG.error(e);
            return null;
        }

    }

    public static <T> List<T> getList(Class clazz, Map myRestrictions, Integer numberOfValues, String sort){
        try{
            return HibernateUtil.getInstance().getList(clazz,myRestrictions, numberOfValues, sort);
        } catch (TransactionException e){
            LOG.error(e);
            return null;
        }
    }

    public static <T> List<T> getList(Class<T> clazz, Map myRestrictions) {
        try{
            return HibernateUtil.getInstance().getList(clazz, myRestrictions);
        } catch (TransactionException e){
            LOG.error(e);
            return null;
        }
    }

    public static Long getCount(Class clazz){
        try{
            return HibernateUtil.getInstance().getCount(clazz);
        } catch (TransactionException e){
            LOG.error(e);
            return null;
        }
    }
}
