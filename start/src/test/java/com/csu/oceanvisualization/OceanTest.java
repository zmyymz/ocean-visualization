package com.csu.oceanvisualization;

import com.google.common.collect.ImmutableList;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization
 * @date 2022/1/28 14:46
 */
public class OceanTest {
    @Test
    public void test01() throws IOException {
        // 0° 设置时间
        String timeStart = "2018-01-01 00:30:00";
        String timeEnd = "2018-01-01 00:35:00";
        // 1° 设置原始文件路径+转存文件路径
        String filePath = "D:/OceanVisualization/data/SSH_202104.nc";
        String savePath = "/Users/caowei/workspace/test.nc";
        // 2° 读取文件
        NetcdfFile ncFile = NetcdfFile.open(filePath, null);
        // 3° 设置时间变量
        Variable timeObject = ncFile.findVariable("time");
        String timeAt = timeObject.getUnitsString();
        String regex = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{1}.\\d{1}";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(timeAt);
        if (matcher.find()) {
            timeAt = matcher.group(0);
        }
        String curDate = timeAt.split(" ")[0]; // 1858-11-17
        System.out.println(timeAt);
        System.out.println(curDate);

        Variable ssh_pred = ncFile.findVariable("SSH_pred");
        System.out.println(ssh_pred.getElementSize());
        Attribute ssh_pred1 = ncFile.findAttribute("SSH_pred");
        System.out.println(ssh_pred1);
    }


    /**
     * 给计算定日期以及天数(小时)后的日期
     *
     * @param sDate
     * @param iDate
     * @param iCal
     * @param sStr
     * @return
     */
    public String getNextDate(String sDate, int iDate, int iCal, String sStr) {
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

    @Test
    public void test03() throws ParseException {
        List<Integer> timeRangeList = Arrays.asList(1054176, 1054177, 1054178, 1054179, 1054180, 1054181, 1054182,
                1054183, 1054184, 1054185, 1054186, 1054187, 1054188, 1054189, 1054190,
                1054191, 1054192, 1054193, 1054194, 1054195, 1054196, 1054197, 1054198,
                1054199, 1054200, 1054201, 1054202, 1054203, 1054204, 1054205, 1054206,
                1054207, 1054208, 1054209, 1054210, 1054211, 1054212, 1054213, 1054214,
                1054215, 1054216, 1054217, 1054218, 1054219, 1054220, 1054221, 1054222,
                1054223, 1054224, 1054225, 1054226, 1054227, 1054228, 1054229, 1054230,
                1054231, 1054232, 1054233, 1054234, 1054235, 1054236, 1054237, 1054238,
                1054239, 1054240, 1054241, 1054242, 1054243, 1054244, 1054245, 1054246,
                1054247, 1054248, 1054249, 1054250, 1054251, 1054252, 1054253, 1054254,
                1054255, 1054256, 1054257, 1054258, 1054259, 1054260, 1054261, 1054262,
                1054263, 1054264, 1054265, 1054266, 1054267, 1054268, 1054269, 1054270,
                1054271, 1054272, 1054273, 1054274, 1054275, 1054276, 1054277, 1054278,
                1054279, 1054280, 1054281, 1054282, 1054283, 1054284, 1054285, 1054286,
                1054287, 1054288, 1054289, 1054290, 1054291, 1054292, 1054293, 1054294,
                1054295);
        ArrayList<String> timeResultList = Lists.newArrayList();
        HashSet<String> set = new HashSet<>();


        for (Integer time : timeRangeList) {
            String nextDate = getNextDate("1900-01-01", time, Calendar.HOUR_OF_DAY, "yyyy-MM-dd");
            timeResultList.add(nextDate);
            set.add(nextDate);
        }
        System.out.println(timeResultList);
        System.out.println(set);


        System.out.println(getStringToDate("1900-01-01 00:00:0.0"));

        String dateToString = getDateToString(-2209017600000L);
        System.out.println(dateToString);


        // wave_direction
        // for (Integer time : timeRangeList) {
        //     String nextDate = getNextDate("1900-01-01 00:00:0.0", time, Calendar.HOUR_OF_DAY, "yyyy-MM-dd HH:mm:ss");
        //     timeResultList.add(nextDate);
        //     set.add(nextDate);
        // }
        // System.out.println(timeResultList);
        // System.out.println(set);


        // String curDate = getNextDate("1858-11-17", 59310, Calendar.DATE, "yyyy-MM-dd");
        // 0000-01-01   738405
        // String curDate = getNextDate("0000-01-01", 738405, Calendar.DATE, "yyyy-MM-dd");

        // System.out.println(curDate);


        // String str1 = "2013-08-11";
        // String str2 = "2013-08-15";
        // SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // Date d1 = simpleDateFormat.parse(str1);
        // Date d2 = simpleDateFormat.parse(str2);
        // int day = daysOfTwo(d1, d2);
        // System.out.println(day);
    }

}
