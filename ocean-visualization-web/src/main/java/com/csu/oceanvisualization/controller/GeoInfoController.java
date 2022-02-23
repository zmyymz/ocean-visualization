package com.csu.oceanvisualization.controller;

import com.csu.oceanvisualization.commonutils.Result;
import com.csu.oceanvisualization.service.GeoJsonDataService;
import com.csu.oceanvisualization.servicebase.exceptionhandler.OceanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.controller
 * @date 2022/2/19 15:36
 */
@RestController
@RequestMapping("/typhoon")
@CrossOrigin
public class GeoInfoController {
    @Autowired
    private GeoJsonDataService geoJsonDataService;

    @GetMapping("getGeojsonData")
    public Result getGeoJsonData() throws ExecutionException {
        // String geoJsonData;
        // try {
        //     geoJsonData = geoJsonDataService.getGeoJsonData();
        // } catch (Exception e) {
        //     throw new OceanException(20001, "txt转geojson出现异常");
        // }

        String geoJsonData = geoJsonDataService.getGeoJsonData();
        return Result.ok().data("geoJsonData", geoJsonData);
    }
}
