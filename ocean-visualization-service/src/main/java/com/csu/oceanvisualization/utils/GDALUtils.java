package com.csu.oceanvisualization.utils;

import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import org.junit.platform.commons.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.utils
 * @date 2022/2/18 14:01
 */
@Slf4j
public class GDALUtils {
    // swh.nc -> .tif
    public static void gdalTranslate(String inputFilePath, String outputFilePath) throws IOException {
        // 1° 设置原始文件路径+转存文件路径
        String filePath = inputFilePath;

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
        // String unitsString = timeObject.getUnitsString();
        String unitsString = timeObject.getUnitsString();
        if (StringUtils.isBlank(unitsString)) {
            ImmutableList<Attribute> globalAttributes = ncFile.getGlobalAttributes();
            unitsString = globalAttributes.get(0).toString();
        }
        String originUnitsString = unitsString;
        String regex = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{1}.\\d{1}";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(unitsString);
        if (matcher.find()) {
            unitsString = matcher.group(0);
        }
        // else{
        //     String regex2 = "\\d{4}-\\d{2}-\\d{2}";
        //     Pattern p2 = Pattern.compile(regex2);
        //     Matcher matcher2 = p2.matcher(unitsString);
        //     if(matcher2.find()){
        //         unitsString = matcher2.group(0) + " 00:00:0.0";
        //     }
        // }
        // unitsString 1858-11-17 00:00:0.0
        String[] timeArray = unitsString.split(" ");
        // 日期: 1858-11-17
        String curDate = timeArray[0];
        // 时间: 00:00:0.0
        String curTime = timeArray[1];
        // System.out.println(curDate);
        // System.out.println(curTime);

        ArrayList<String> timestampList = new ArrayList<>();
        String nextDate;
        for (Integer time : timeList) {
            if (originUnitsString.contains("hours")) {
                nextDate = DateUtils.getNextDate(unitsString, time, Calendar.HOUR_OF_DAY, "yyyy-MM-dd HH:mm:ss");
            } else {
                nextDate = DateUtils.getNextDate(unitsString, time, Calendar.DATE, "yyyy-MM-dd HH:mm:ss");
            }
            long stringToDate = DateUtils.getStringToDate(nextDate);
            timestampList.add(String.valueOf(stringToDate));
        }


        //如果文件夹不存在则创建
        File file = new File(outputFilePath);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdir();
        }

        String commandStr;
        for (int i = 0; i < timeSize; i++) {
            for (String variable : variablesNameList) {
                if (inputFilePath.contains("wave_direction")) {
                    commandStr = "cmd /c gdal_translate -projwin_srs epsg:4326 -a_ullr 104.75 -0.25 122.25 25.25 -a_nodata 0 -b " + (i + 1) + " NETCDF:\"" + filePath + "\":" + variable + " " + outputFilePath + variable + "_" + timestampList.get(i) + ".tif";
                    CMDUtils.executeCMD(commandStr);
                } else {
                    commandStr = "cmd /c gdal_translate -projwin_srs epsg:4326 -b " + (i + 1) + " NETCDF:\"" + filePath + "\":" + variable + " " + outputFilePath + variable + "_" + timestampList.get(i) + ".tif";
                    CMDUtils.executeCMD(commandStr);
                }
                // System.out.println(commandStr);
            }
        }
    }
}
