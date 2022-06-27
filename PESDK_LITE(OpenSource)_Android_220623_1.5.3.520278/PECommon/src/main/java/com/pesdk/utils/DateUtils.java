package com.pesdk.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 */
public class DateUtils {

    /**
     * 时间
     */
    private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    public static String longToDate(long st){
        Date date=new Date(st);
        return sdf.format(date);

    }

    public static long dateToLong(String string){
        try {
            return sdf.parse(string).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

}
