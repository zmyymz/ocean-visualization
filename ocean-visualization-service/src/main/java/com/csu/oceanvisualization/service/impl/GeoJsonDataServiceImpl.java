package com.csu.oceanvisualization.service.impl;

import com.csu.oceanvisualization.service.GeoJsonDataService;
import com.csu.oceanvisualization.service.ParseCSVToGeoJson;
import com.csu.oceanvisualization.utils.CMDUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.service.impl
 * @date 2022/2/19 15:45
 */
@Service
@Slf4j
public class GeoJsonDataServiceImpl implements GeoJsonDataService {

    @Autowired
    private Map<String, ParseCSVToGeoJson> parseCSVToGeoJsonMap;

    public static LoadingCache<String, String> cache;

    @SneakyThrows
    @Override
    public String getGeoJsonData(String fourOceans, String tpSeq) {
        ParseCSVToGeoJson parseBySerialize = parseCSVToGeoJsonMap.get("parseBySerialize");

        String geoJsonString = null;
        if (cache != null) {
            geoJsonString = cache.get(tpSeq);
        } else {
            cache = CacheBuilder.newBuilder()
                    .recordStats()
                    .maximumSize(1000)
                    .expireAfterAccess(10, TimeUnit.DAYS)
                    .build(new CacheLoader<String, String>() {
                        @Override
                        public String load(String tpSeq) throws Exception {
                            System.out.println("cache 执行了");
                            return parseBySerialize.parse(fourOceans, tpSeq);
                        }
                    });
            geoJsonString = cache.get(tpSeq);
        }
        return geoJsonString;
    }


    // @Override
    // public String getGeoJsonData() throws ExecutionException {
    //     LoadingCache<String, String> cache = CacheBuilder.newBuilder()
    //             .recordStats()
    //             .maximumSize(1000)
    //             .expireAfterAccess(10, TimeUnit.DAYS)
    //             .build(new CacheLoader<String, String>() {
    //                 @Override
    //                 public String load(String s) throws Exception {
    //                     System.out.println("cache 执行了");
    //                     return getGeoJsonDataFromFile(s);
    //                 }
    //             });
    //
    //     String geoJsonString = cache.get("geojson");
    //     return geoJsonString;
    // }
    //
    // public String getGeoJsonDataFromFile(String key) {
    //
    //     String geoJsonString;
    //     // 拼接python命令
    //     String property = System.getProperties().getProperty("os.name");
    //     // 执行 windows cmd
    //     String inputPath = "D:/OceanVisualization/data/typhoon_data/WP_solo/tp_seq1.txt";
    //     // 可以传参, 暂时不用
    //     String outputPath = "D:/out.geojson";
    //     String scriptPath = "D:\\Java\\JavaEE\\IdeaProjects\\ocean-visualization\\ocean-visualization-service\\src\\main\\java\\com\\csu\\oceanvisualization\\scripts\\txt2geojson.py";
    //     // String geoJsonString;
    //     if (property.toLowerCase().startsWith("win")) {
    //         String commandStr = "cmd /c python " + scriptPath + " " + inputPath + " " + outputPath;
    //         log.info("windows 执行 " + commandStr);
    //         geoJsonString = CMDUtils.executeCMD(commandStr);
    //         System.out.println(geoJsonString);
    //
    //     } else {
    //         // 执行 linux cmd
    //         String commandStr = "python " + scriptPath + inputPath + outputPath;
    //         geoJsonString = CMDUtils.executeCMD(commandStr);
    //     }
    //     return geoJsonString;
    // }
}
