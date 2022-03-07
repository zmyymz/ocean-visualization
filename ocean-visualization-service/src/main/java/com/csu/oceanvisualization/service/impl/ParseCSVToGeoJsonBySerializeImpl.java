package com.csu.oceanvisualization.service.impl;

import com.csu.oceanvisualization.entity.Feature;
import com.csu.oceanvisualization.entity.GeoJsonFeature;
import com.csu.oceanvisualization.entity.Geometry;
import com.csu.oceanvisualization.entity.TyphoonProperty;
import com.csu.oceanvisualization.service.ParseCSVToGeoJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.service.impl
 * @date 2022/2/23 19:51
 */
@Service("parseBySerialize")
public class ParseCSVToGeoJsonBySerializeImpl implements ParseCSVToGeoJson {
    @Value("${com.csu.typhoon.serverfile-path}")
    private String serverTempFilePath;

    @SneakyThrows
    @Override
    public String parse(String fourOceans, String tpSeq) {
        System.out.println("ParseCSVToGeoJson#parse 执行了");
        String csvFilePath = FilenameUtils.separatorsToSystem(serverTempFilePath + fourOceans.toUpperCase() + "_solo/" + tpSeq.trim() + ".txt");
        System.out.println(csvFilePath);
        // String csvFilePath = "D:\\OceanVisualization\\data\\typhoon_data\\WP_solo\\" + tpSeq.trim() + ".txt";
        // String csvFilePath = "D:\\OceanVisualization\\data\\typhoon_data\\WP_solo\\tp_seq1.txt";
        ArrayList<Feature> featureArrayList = new ArrayList<>();

        try (BufferedReader in = new BufferedReader(new FileReader(csvFilePath));) {
            // processing code here
            String s = null;
            StringBuilder sb = new StringBuilder();
            int id = 0;
            while ((s = in.readLine()) != null) {
                String[] line = s.split(" ");
                Double longitude = Double.valueOf(line[2]);
                Double latitude = Double.valueOf(line[3]);
                Integer minPressure = Integer.valueOf(line[4]);
                Integer maxWindSpeed = Integer.valueOf(line[5]);
                Geometry geometry = new Geometry();
                geometry.setType("Point");
                geometry.setCoordinates(Arrays.asList(longitude, latitude));

                Feature feature = new Feature();
                feature.setId(String.valueOf(id));
                id++;
                feature.setType("Feature");
                feature.setGeometry(geometry);

                TyphoonProperty properties = new TyphoonProperty();
                properties.setDate(line[0]);
                properties.setLatitude(latitude);
                properties.setLongitude(longitude);
                properties.setMaxWindSpeed(maxWindSpeed);
                properties.setMinPressure(minPressure);
                properties.setTime(line[1]);

                feature.setProperties(properties);
                featureArrayList.add(feature);
            }
        }
        GeoJsonFeature geoJsonFeature = new GeoJsonFeature();
        geoJsonFeature.setType("FeatureCollection");
        geoJsonFeature.setFeatureList(featureArrayList);
        // System.out.println(geoJsonFeature);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(geoJsonFeature);
        return json;
    }
}