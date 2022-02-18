package com.csu.oceanvisualization.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.utils
 * @date 2022/2/18 14:03
 */
public class DateUtils {
    /**
     * 给计算定日期以及天数(小时)后的日期
     *
     * @param sDate
     * @param iDate
     * @param iCal
     * @param sStr
     * @return
     */
    public static String getNextDate(String sDate, int iDate, int iCal, String sStr) {
        String sNextDate = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(sStr);
        Date date = null;
        try {
            date = formatter.parse(sDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.setTime(date);
        calendar.add(iCal, iDate);
        sNextDate = formatter.format(calendar.getTime());
        return sNextDate;
    }


    public static int daysOfTwo(Date fDate, Date oDate) {
        Calendar aCalendar = Calendar.getInstance();
        aCalendar.setTime(fDate);
        int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);
        aCalendar.setTime(oDate);
        int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);
        return day2 - day1;
    }

    /**
     * 将字符串转为时间戳
     */
    public static long getStringToDate(String time) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        try {
            date = sf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    /**
     * 时间戳转换成字符串
     */
    public static String getDateToString(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sf.format(d);
    }
}
