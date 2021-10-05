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

package co.luism.lukisoftiot.utils;


import co.luism.lukisoftiot.enterprise.SnapShotAlarmTagValue;
import me.jaksa.namedparameters.Param;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.*;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.service.ServiceRegistry;
import java.util.List;
import java.util.Map;

import static me.jaksa.namedparameters.Params.getParam;
import static me.jaksa.namedparameters.Params.param;


public class HibernateUtil {

    private static SessionFactory sessionFactory;
    private static final Logger LOG = Logger.getLogger(HibernateUtil.class);
    private static HibernateUtil  instance = null;
    private HibernateUtil(){
        sessionFactory = buildSessionFactory();
    }


    public static HibernateUtil getInstance(){
        if(instance == null){
            instance = new HibernateUtil();
        }

        return instance;
    }

    private static final String database = "lukiiot";


    public static void saveCredentials(String user_name, String user_password){

        Configuration configuration = new Configuration();

        configuration.setProperty("hibernate.connection.username", user_name);
        configuration.setProperty("hibernate.connection.password", user_password);
        String connectionString = "jdbc:mysql://localhost:3306/"  + HibernateUtil.database;
        configuration.setProperty("hibernate.connection.url", connectionString);
        configuration.configure();

    }



	private static SessionFactory buildSessionFactory() throws ExceptionInInitializerError
    {
		try {
			// Use hibernate.cfg.xml to get a SessionFactory
			//return new Configuration().configure().buildSessionFactory();
			//return new AnnotationConfiguration().configure().buildSessionFactory();

            Configuration configuration = new Configuration();
            configuration.configure();
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(
                    configuration.getProperties()).build();
            sessionFactory = configuration.buildSessionFactory(serviceRegistry);
            return sessionFactory;


		} catch (Exception ex) {
            LOG.error("SessionFactory creation failed." + ex.getMessage());
            return null;
        }

	}


	public void shutdown() {
        sessionFactory.close();
	}

    public void sendQuery(){
        // Non-managed environment idiom
        Session mySession = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = mySession.beginTransaction();



            tx.commit();
        }
        catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
            }
            LOG.error(e);
        }
        finally {
            mySession.close();
        }

    }

    public boolean sendExecuteUpdate(String s) {

        // Non-managed environment idiom
        Session mySession = sessionFactory.openSession();
        Transaction tx = null;
        int result = 0;
        try {
            tx = mySession.beginTransaction();
            result = mySession.createSQLQuery(s).executeUpdate();
            tx.commit();
        }
        catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
                result = 1;
            }
            LOG.error(e);
        }
        finally {
            mySession.close();
        }

        return result >= 0;
    }

    public boolean sendExecuteCreate(Object o) {

        boolean ok = false;
        // Non-managed environment idiom
        Session mySession = sessionFactory.openSession();
        Transaction tx = null;
        int result = 0;
        try {
            tx = mySession.beginTransaction();
            mySession.save(o);
            tx.commit();
            ok = true;
        } catch (ConstraintViolationException e){
            LOG.error("Error on create:" + e.getMessage());
            ok = false;

        } catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
            }
            LOG.error(e);
            ok = false;
        }
        finally {
            mySession.close();
        }

        return ok;
    }

    public <T> T sendExecuteRead(Class clazz, int id){

        Session mySession = sessionFactory.openSession();
        T dbObject = null;
        Transaction tx = null;
        try{
            tx = mySession.beginTransaction();
            dbObject = (T) mySession.get(clazz, id);
            tx.commit();


        } catch (HibernateException e){
            LOG.error("Value not found:" + e.getMessage());
            if (tx != null) {
                tx.rollback();
            }


        } finally {


            mySession.close();


        }

        return  dbObject;
    }

    public  <T> T sendExecuteRead(Class clazz, String propertyName, Object propertyValue){

        Session mySession = sessionFactory.openSession();


        T dbObject = null;
        Transaction tx = null;

        try{
            tx = mySession.beginTransaction();
            dbObject = (T) mySession.createCriteria(clazz).add(Restrictions.eq(propertyName, propertyValue)).uniqueResult();
            tx.commit();
            mySession.flush();

        } catch (HibernateException e){
            LOG.error("Value not found:" + e.getMessage());
            if (tx != null) {
                tx.rollback();
            }


        } finally {


            mySession.close();


        }

        return  dbObject;
    }

    public <T> T sendExecuteRead(Class clazz, Map myRestriction){

        Session mySession = sessionFactory.openSession();


        T dbObject = null;
        Transaction tx = null;

        try{
            tx = mySession.beginTransaction();
            dbObject = (T) mySession.createCriteria(clazz).add(Restrictions.allEq(myRestriction)).uniqueResult();
            tx.commit();
            mySession.flush();

        } catch (HibernateException e){
            LOG.error("Value not found:" + e.getMessage());
            if (tx != null) {
                tx.rollback();
            }


        } finally {


            mySession.close();


        }

        return  dbObject;
    }

    public String sendExecuteReadSingleProperty(Class clazz, String propertyName, String propertyValue, String returnValue){
        Session mySession = sessionFactory.openSession();


        Object dbObject = null;
        Transaction tx = null;

        try{
            tx = mySession.beginTransaction();
            dbObject =  mySession.createCriteria(clazz).add(Restrictions.eq(propertyName, propertyValue))
                    .setProjection(Projections.property(returnValue)).uniqueResult();
            tx.commit();
            mySession.flush();

        } catch (HibernateException e){
            LOG.error("Value not found:" + e.getMessage());
            if (tx != null) {
                tx.rollback();
            }


        } finally {


            mySession.close();


        }

        if(dbObject instanceof String){
            return (String) dbObject;
        }

        return  "";
    }

    public boolean sendExecuteUpdate(Object o) {
        boolean ok= false;
        Session mySession = sessionFactory.openSession();
        Transaction tx = null;

        if(o == null){
            return false;
        }

        try{
            tx = mySession.beginTransaction();
            mySession.update(o);
            tx.commit();
            mySession.flush();
            ok = true;
        }catch(ConstraintViolationException | TransientObjectException e) {
            LOG.error("Cannot update value:" + e.getMessage());
            if (tx != null) {
                tx.rollback();
            }
        } finally {

            mySession.close();
        }
        return ok;
    }

    public boolean sendExecuteDelete(Object o) {
        boolean ok = false;
        Session mySession = sessionFactory.openSession();
        Transaction tx = null;


        try{
            tx = mySession.beginTransaction();
            mySession.delete(o);
            tx.commit();
            mySession.flush();
            ok = true;

        } catch (HibernateException e){
            LOG.error("Value not found:" + e.getMessage());
            if (tx != null) {
                tx.rollback();
            }

        } finally {
            mySession.close();
        }
        return  ok;
    }

    public <T> List<T> getList(Class clazz){

        Session mySession = sessionFactory.openSession();
        Transaction tx = null;

        List<T> dbObject = null;

        try{
            tx = mySession.beginTransaction();
            dbObject = (List<T>) mySession.createCriteria(clazz).list();
            tx.commit();
            mySession.flush();

        } catch (HibernateException e){
            LOG.error("List not found:" + e.getMessage());
            if (tx != null) {
                tx.rollback();
            }


        } finally {
            mySession.close();
        }

        return  dbObject;

    }

    @SuppressWarnings("unchecked")
    public  <T> List<T> getList(Class clazz, Map myRestrictions, Integer numberOfValues, String sort) {

        Session mySession = sessionFactory.openSession();
        Transaction tx = null;

        List dbObject = null;

        //"select userName from AccountInfo order by points desc 5";

        try{
            tx = mySession.beginTransaction();

            Criteria cr = mySession.createCriteria(clazz);
            cr.add(Restrictions.allEq(myRestrictions));
            cr.addOrder(Order.desc(sort));
            cr.setMaxResults(numberOfValues);
            dbObject = cr.list();
            tx.commit();
            mySession.flush();

        } catch (HibernateException e){
            LOG.error("List not found:" + e.getMessage());
            if (tx != null) {
                tx.rollback();
            }


        } finally {


            mySession.close();


        }

        return  dbObject;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(Class<T> clazz, Map myRestrictions) {

        Session mySession = sessionFactory.openSession();
        Transaction tx = null;
        List dbObject = null;

        //"select userName from AccountInfo order by points desc 5";
        try{
            tx = mySession.beginTransaction();
            Criteria cr = mySession.createCriteria(clazz);
            cr.add(Restrictions.allEq(myRestrictions));
            dbObject = cr.list();
            tx.commit();
            mySession.flush();

        } catch (HibernateException e){
            LOG.error("List not found:" + e.getMessage());
            if (tx != null) {
                tx.rollback();
            }

        } finally {


            mySession.close();


        }

        return  dbObject;
    }

    public <T> Long getCount(Class clazz){

        Session mySession = sessionFactory.openSession();
        Transaction tx = null;

        Number dbObject = null;

        try{
            tx = mySession.beginTransaction();


            dbObject = (Number) mySession.createCriteria(clazz).setProjection(Projections.rowCount()).uniqueResult();

            tx.commit();
            mySession.flush();

        } catch (HibernateException e){
            LOG.error("List not found:" + e.getMessage());
            if (tx != null) {
                tx.rollback();
            }


        } finally {


            mySession.close();


        }

        return dbObject != null ? dbObject.longValue() : null;

    }


    public boolean sendExecuteDelete(Class clazz, Map myRestriction) {

        boolean ok = false;
        if(myRestriction.size() > 1){
            LOG.error("delete with more then one restriction is not implemented");
            return false;
        }
        Session mySession = sessionFactory.openSession();
        Transaction tx = null;

        try{
            tx = mySession.beginTransaction();

            StringBuilder sb = new StringBuilder(String.format("delete from %s where ", clazz.getName()));

            for(Object s : myRestriction.keySet()){
                if(s instanceof String){
                    sb.append(String.format("%s= :%s", s, s));
                }
            }

            String hql = sb.toString();

            Query q  = mySession.createQuery(hql);

            for(Object s : myRestriction.keySet()){
                if(s instanceof String){
                   q.setString((String)s, myRestriction.get(s).toString());
                }
            }

            q.executeUpdate();
            tx.commit();
            mySession.flush();
            ok = true;

        } catch (HibernateException e){
            LOG.error("Value not deleted:" + e);
            if (tx != null) {
                tx.rollback();
            }


        } finally {


            mySession.close();


        }

        return  ok;


    }



    public <T> boolean sendExecuteUpdateValue(Class clazz, String vehicleId, List<Object> myTagIdRestriction, long value) {

        boolean ok = false;

        Session mySession = sessionFactory.openSession();
        Transaction tx = null;

        try{
            tx = mySession.beginTransaction();

            Criteria criteria = mySession.createCriteria(clazz);

//          (((A='X') and (B in('X',Y))) or ((A='Y') and (B='Z')))
//            Criterion rest1= Restrictions.and(Restrictions.eq("A", "X"),
//                    Restrictions.in("B", Arrays.asList("X","Y")));
//            Criterion rest2= Restrictions.and(Restrictions.eq("A", "Y"),
//                    Restrictions.eq("B", "Z"));
//            criteria.add(Restrictions.or(rest1, rest2));
//            Junction conditionGroup = Restrictions.disjunction();
//            conditionGroup.add(condition1).add(condition2).add(condition3);
//            criteria.add(conditionGroup);
            Junction conditionGroup = Restrictions.disjunction();

            for(Object o : myTagIdRestriction){

                conditionGroup.add(Restrictions.eq("tagId", o));
            }


            Criterion a = Restrictions.eq("vehicleId", vehicleId);
            Criterion b = Restrictions.and(a, conditionGroup);

            criteria.add(b);
            ScrollableResults items = criteria.scroll();

            int count = 0;
            while ( items.next() ) {
                Object e = items.get(0);

                if(e instanceof SnapShotAlarmTagValue){
                    ((SnapShotAlarmTagValue)e).setValue(value);
                    mySession.saveOrUpdate(e);
                }

                if (++count % 100 == 0) {
                    mySession.flush();
                    mySession.clear();
                }
            }


            tx.commit();
            mySession.flush();
            ok = true;

        } catch (HibernateException e){
            LOG.error("Value not deleted:" + e);
            if (tx != null) {
                tx.rollback();
            }


        } finally {
            mySession.close();
        }
        return  ok;


    }






}
