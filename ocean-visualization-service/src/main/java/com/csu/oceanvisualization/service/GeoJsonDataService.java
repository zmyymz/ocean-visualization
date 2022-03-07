package com.csu.oceanvisualization.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.service
 * @date 2022/2/19 19:34
 */

public interface GeoJsonDataService {
    public String getGeoJsonData(String fourOceans, String tpSeq) throws ExecutionException;
}
