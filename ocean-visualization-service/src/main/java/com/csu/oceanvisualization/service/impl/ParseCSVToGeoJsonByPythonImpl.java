package com.csu.oceanvisualization.service.impl;

import com.csu.oceanvisualization.service.ParseCSVToGeoJson;
import com.csu.oceanvisualization.utils.CMDUtils;
import org.springframework.stereotype.Service;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.service
 * @date 2022/2/23 19:49
 */
@Service("parseByPython")
public class ParseCSVToGeoJsonByPythonImpl implements ParseCSVToGeoJson {
    @Override
    public String parse(String fourOceans, String tpSeq) {
        // 拼接python命令
        String property = System.getProperties().getProperty("os.name");
        // 执行 windows cmd
        String inputPath = "D:/OceanVisualization/data/typhoon_data/WP_solo/tp_seq1.txt";
        String outputPath = "D:/out.geojson";
        String scriptPath = "D:\\Java\\JavaEE\\IdeaProjects\\ocean-visualization\\ocean-visualization-service\\src\\main\\java\\com\\csu\\oceanvisualization\\scripts\\txt2geojson.py";
        String geoJsonString;
        if (property.toLowerCase().startsWith("win")) {
            String commandStr = "cmd /c python " + scriptPath + " " + inputPath + " " + outputPath;
            geoJsonString = CMDUtils.executeCMD(commandStr);
        } else {
            // 执行 linux cmd
            String commandStr = "python " + scriptPath + inputPath + outputPath;
            geoJsonString = CMDUtils.executeCMD(commandStr);
        }
        return geoJsonString;
    }
}
