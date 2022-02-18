package com.csu.oceanvisualization;

import com.csu.oceanvisualization.utils.DateUtils;
import com.csu.oceanvisualization.utils.GDALUtils;
import com.google.common.collect.ImmutableList;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
        // 1° 设置原始文件路径+转存文件路径
        String filePath = "D:/OceanVisualization/data/SSH_202104.nc";
        // String savePath = "/Users/caowei/workspace/test.nc";
        // 2° 读取文件
        // NetcdfFile netcdfFile = NetcdfFiles.open(filePath);
        NetcdfFile ncFile = NetcdfFile.open(filePath, null);

        // 获取所有的变量名
        ImmutableList<Variable> variablesList = ncFile.getVariables();
        ImmutableList<Variable> variablesShortNameList;
        ArrayList<String> variablesNameList = new ArrayList<>();
        for (Variable variable : variablesList) {
            String shortName = variable.getShortName();
            // System.out.println(shortName);
            if (shortName.equals("lat") || shortName.equals("lon") || shortName.equals("time"))
                continue;
            String variableTempName = shortName;
            variablesNameList.add(shortName);
        }

        // 设置时间变量
        Variable timeObject = ncFile.findVariable("time");
        // 读取真实时间值 59310.0 59311.0 59312.0 59313.0 59314.0
        Array read = timeObject.read();
        long timeSize = read.getSize();
        String[] period = read.toString().split(" ");
        ArrayList<Integer> timeList = new ArrayList<>();
        for (String s : period) {
            // [59310, 59311, 59312, 59313, 59314]
            timeList.add(Double.valueOf(s).intValue());
        }
        String timeAt = timeObject.getUnitsString();
        String regex = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{1}.\\d{1}";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(timeAt);
        if (matcher.find()) {
            timeAt = matcher.group(0);
        }
        // timeAt 1858-11-17 00:00:0.0
        String[] timeArray = timeAt.split(" ");
        // 日期: 1858-11-17
        String curDate = timeArray[0];
        // 时间: 00:00:0.0
        String curTime = timeArray[1];
        System.out.println(curDate);
        System.out.println(curTime);

        ArrayList<String> timestampList = new ArrayList<>();
        for (Integer time : timeList) {
            String nextDate = DateUtils.getNextDate(timeAt, time, Calendar.DATE, "yyyy-MM-dd HH:mm:ss");
            long stringToDate = DateUtils.getStringToDate(nextDate);
            timestampList.add(String.valueOf(stringToDate));
        }

        for (int i = 0; i < timeSize; i++) {
            for (String variable : variablesNameList) {
                String commandStr = "gdal_translate -projwin_srs epsg:4326 -sds -b " + (i + 1) + " NETCDF:\"" + filePath + "\":" + variable + " " + variable + "_" + timestampList.get(i) + ".tif";
                System.out.println(commandStr);
            }
        }
    }


    @Test
    public void testgdalTranslate() throws IOException {
        GDALUtils.gdalTranslate("D:/OceanVisualization/data/SWH.nc", "D:/");
        // gdalTranslate("D:/OceanVisualization/data/wave_direction.nc", "D:/");
        // System.out.println(getDateToString(88881984000000L));
    }


    @Test
    public void test03() throws ParseException {
        // wave_direction时间测试
        List<Integer> timeRangeList = Arrays.asList(1054176, 1054177, 1054178, 1054179, 1054180, 1054181, 1054182);
        ArrayList<String> timeResultList = Lists.newArrayList();
        // HashSet<String> set = new HashSet<>();
        for (Integer time : timeRangeList) {
            String nextDate = DateUtils.getNextDate("1900-01-01", time, Calendar.HOUR_OF_DAY, "yyyy-MM-dd");
            timeResultList.add(nextDate);
            // set.add(nextDate);
        }
        System.out.println(timeResultList);
        // System.out.println(set);


        // 台风数据测试
        // String nextDate = getNextDate("1900-01-01 00:00:0.0", 692520, Calendar.HOUR_OF_DAY, "yyyy-MM-dd HH:mm:ss");
        // System.out.println(nextDate);


        // System.out.println(getStringToDate("1900-01-01 00:00:0.0"));
        //
        // String dateToString = getDateToString(-2209017600000L);
        // System.out.println(dateToString);


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

    /**
     * 时间互转测试
     */
    @Test
    public void testTimeStamps(){
        System.out.println(DateUtils.getDateToString(1586016000000L));
    }

    /**
     * 执行简单cmd命令
     * @param command
     */
    public void executeGdalTranslate(String command) {
        BufferedReader br = null;
        try {
            // Process p = Runtime.getRuntime().exec(command);
            // Process p = new ProcessBuilder(command).start();
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            System.out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试简单命令行
     */
    @Test
    public void testCommand() {
        // 拼接gdal命令
        // String commandStr = "ping www.baidu.com";
        String commandStr = "cmd /c gdal_translate -projwin_srs epsg:4326 -b 2 NETCDF:\"D:/SWH.nc\":SWH_Real D:/SWH_22222_Real.tif";
        executeGdalTranslate(commandStr);
    }


    @Test
    public void testCopyFile() {

    }

}
