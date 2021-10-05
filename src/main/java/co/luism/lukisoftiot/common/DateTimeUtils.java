package co.luism.lukisoftiot.common;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by luis on 29.01.15.
 */
public class DateTimeUtils {

    public static String splitToComponentTimes(int seconds)
    {

        int hours = seconds / 3600;
        int remainder = seconds - hours * 3600;
        int minutes = remainder / 60;
        remainder = remainder - minutes * 60;
        int secs = remainder;

        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /* CONVERT TIME STAMP TO STRING*/


    public static String getTimeStringUtc(Integer v){

        TimeZone tz = TimeZone.getTimeZone("UTC");

        return getDateTimeFromTimeStamp(v * 1000L, tz) + tz.getDisplayName(false, TimeZone.SHORT);

    }

    public static String getTimeStringUtc(Long v){

        TimeZone tz = TimeZone.getTimeZone("UTC");

        return getDateTimeFromTimeStamp(v, tz) + tz.getDisplayName(false, TimeZone.SHORT);

    }

    public static String getTimeStringLocal(Integer v){

        TimeZone tz = TimeZone.getDefault();
        return getDateTimeFromTimeStamp(v * 1000L,tz) + tz.getDisplayName(true, TimeZone.SHORT);

    }

    public static String getTimeStringLocal(Long v){

        TimeZone tz = TimeZone.getDefault();
        return getDateTimeFromTimeStamp(v,tz) + tz.getDisplayName(true, TimeZone.SHORT);

    }

    public static String getCurrentTimeStringUtc() {
        java.util.Date date= new java.util.Date();
        return getTimeStringUtc(date.getTime());

    }

    public static String getDateTimeFromTimeStamp(Long vLong, TimeZone tz){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        sdf.setTimeZone(tz);
        return sdf.format(new Timestamp(vLong));


    }


    /* CONVERT TIMESTAMP TO INTEGER SECONDS AND MILLISECONDS*/

    public static Integer getSecondsTimeStamp(Timestamp ts){

        Long l = ts.getTime() / 1000;

        return l.intValue();
    }

    public static Integer getMillisecondsTimeStamp(Timestamp ts){

        Long l = ts.getTime() % 1000;

        return l.intValue();
    }

    /* GET TIMESTAMP FROM SQL TS and UNIX TS */

    public static Timestamp getTimeStampFromMilliSecondsTime(Long v){

        return new Timestamp(v);

    }

    public static Timestamp getTimeStampFromSecondsTime(Integer v){

        return new Timestamp(v * 1000L);

    }

    /* GET CURRENT TIMESTAMP TO SQL TS and UNIX TS*/

    public static Timestamp getCurrentTimeStamp(){

        java.util.Date date= new java.util.Date();

        return new Timestamp(date.getTime());

    }

    public static Integer getCurrentTimeStampSeconds(){

        java.util.Date date= new java.util.Date();
        long l = date.getTime();

        return  (int)(l/1000);

    }


}
