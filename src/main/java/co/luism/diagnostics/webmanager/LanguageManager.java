package co.luism.diagnostics.webmanager;
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

import co.luism.diagnostics.common.DiagnosticsPersistent;

import co.luism.diagnostics.enterprise.utils.TranslationIndex;
import co.luism.diagnostics.enterprise.AlarmTagDescription;
import co.luism.diagnostics.enterprise.Language;
import co.luism.diagnostics.enterprise.Translation;
import org.apache.log4j.Logger;

import java.io.*;

import java.util.*;

/**
 * webmanager
 * co.luism.common
 * Created by luis on 09.10.14.
 * Version History
 * 1.00.00 - luis - Initial Version
 */



public class LanguageManager {

    private static final Logger LOG = Logger.getLogger(LanguageManager.class);
    public static final LanguageManager instance = new LanguageManager();
    private static final String defaultLanguage = "en";
    private static String currentLanguage;
    private final Map<String, Language> languageMap = new HashMap<>();
    private final Set<Language> activeLanguages = new TreeSet<>();
    private final List<TranslationIndex> translationIndexList = new ArrayList<>();
    private final Map<TranslationIndex, Translation> translationHashMap = new HashMap<>();
    private final Map<String, Map<Integer, AlarmTagDescription>> tagDescriptionMap = new HashMap<>();

    private LanguageManager(){
        init();
    }

    public static LanguageManager getInstance(){


        return instance;
    }


    private void init() {

        currentLanguage = defaultLanguage;
        List<Language> languageList = DiagnosticsPersistent.getList(Language.class);

        if (languageList == null) return;
        for(Language l : languageList){
            languageMap.put(l.getName(), l);
            if(l.isEnabled()){
                activeLanguages.add(l);
            }

            for(Translation t : l.getTranslationList()) {

                TranslationIndex translationIndex = new TranslationIndex(t.getMyLanguage().getName(), t.getTextId());
                translationIndexList.add(translationIndex);
                translationHashMap.put(translationIndex, t);
            }

        }

        int count = 0;
        List tagDescriptionList = DiagnosticsPersistent.getList(AlarmTagDescription.class);

        for(Object obj: tagDescriptionList){
            if(obj instanceof AlarmTagDescription){
                AlarmTagDescription alarmTagDescription = (AlarmTagDescription) obj;
                Map<Integer, AlarmTagDescription> myMap = tagDescriptionMap.get(alarmTagDescription.getLanguage());
                if(myMap == null){
                    myMap = new HashMap<>();
                }

                myMap.put(alarmTagDescription.getTagId(), alarmTagDescription);
                tagDescriptionMap.put(alarmTagDescription.getLanguage(), myMap);
                count++;

            }
        }

        LOG.info(String.format("Load %d Tag descriptions", count));


    }

    public String getValue(String lang, String field){

        Translation t = findTranslation(lang, field);
        if(t == null){
            return field;
        }

        return t.getTranslation();

    }

    public String getValue(String field){

       return getValue(currentLanguage, field);

    }


    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void setCurrentLanguage(String currentLanguage) {
        this.currentLanguage = currentLanguage;
    }

    public Set<Language> getActiveLanguages() {
        return activeLanguages;
    }

    public Collection<Language> getAllLanguages() {
        return languageMap.values();
    }

    public boolean setEnable(String languageId, boolean enable){
        Language l = languageMap.get(languageId);

        if(l == null){
            return false;
        }

        l.setEnabled(enable);

        if(enable == true){
            activeLanguages.add(l);
        } else {
            activeLanguages.remove(l);
        }

        return true;
    }

    public List<Language> getAllLanguageList() {

        List<Language> languageList = new ArrayList<>();
        languageList.addAll(languageMap.values());
        return languageList;
    }

    public boolean createLanguageFile(Language languageId, File propertyFile){

        InputStream is = null;
        try {
            is = new FileInputStream(propertyFile);
        } catch (FileNotFoundException e) {
            LOG.error(String.format("File not found %s", propertyFile.getName()));
            return false;
        }

        if(storeProperties(languageId, is) == true){

            init();
            return true;
        }

        return false;

    }


    public boolean loadAllTranslations(Class clazz){

        for(Language l : this.languageMap.values()){
            loadTranslationToDatabase(clazz, l);
        }

        return true;
    }

    private boolean loadTranslationToDatabase(Class clazz, Language languageId){


        String path = String.format("lang/translation.%s.properties", languageId.getName());

        InputStream url = LanguageManager.class.getClassLoader().getResourceAsStream(path);

        if(url == null){
            LOG.error(String.format("resource not found %s", path));
            return false;
        }


        return storeProperties(languageId, url);


    }

    private boolean storeProperties(Language languageId, InputStream url) {

        Properties prop = new Properties();
        try {
            //load a properties file from class path, inside static method
            prop.load(url);

            //get all properties
            Set<String> propSet = prop.stringPropertyNames();

            for (String s : propSet) {

                Translation t = findTranslation(languageId.getName(),s);
                if (t == null) {

                    Translation tr = new Translation();
                    tr.setLanguageId(languageId.getLanguageId());
                    tr.setMyLanguage(languageId);
                    tr.setTextId(s);
                    tr.setTranslation(prop.getProperty(s));

                    //create or update
                    tr.create();

                } else {
                    if(!t.getTranslation().equals(prop.getProperty(s))){
                        t.setTranslation(prop.getProperty(s));
                        t.update();
                    } else {
                        LOG.info(String.format("no need to update %s to %s", t.getTranslation(),
                                prop.getProperty(s)));
                    }


                }
            }

        } catch (IOException ex) {
            LOG.error(String.format("resource not found %s", ex.getMessage()));
            return false;
        }

        return true;
    }

    private Translation findTranslation(String Lang, String textId){

        TranslationIndex translationIndex = null;


        for(TranslationIndex ti : translationIndexList){
            if(!ti.languageName.equals(Lang)){
                continue;
            }

            if(!ti.textId.equals(textId)){
                continue;
            }

            translationIndex = ti;
            break;
        }


        if(translationIndex == null){
            LOG.info(String.format("no index found for %s-%s", Lang, textId));
            return null;
        }


        Translation t = translationHashMap.get(translationIndex);

        return t;

    }

    public List<Translation> getAllTranslationList(){
        List<Translation> tr = new ArrayList<>();

        for(Language l : this.languageMap.values()){
            tr.addAll(l.getTranslationList());
        }

        return tr;
    }

    public void addDescription(AlarmTagDescription alarmTagDescription){

        Map<Integer, AlarmTagDescription> myMap = this.tagDescriptionMap.get(alarmTagDescription.getLanguage());
        if(myMap == null){
            myMap = new HashMap<>();
        }
        myMap.put(alarmTagDescription.getTagId(), alarmTagDescription);


    }

    public AlarmTagDescription getTagAlarmDescription(String language, Integer tagId){
        Map<Integer, AlarmTagDescription> myMap = this.tagDescriptionMap.get(language);
        if(myMap == null){
           return null;
        }

        return myMap.get(tagId);
    }


}
