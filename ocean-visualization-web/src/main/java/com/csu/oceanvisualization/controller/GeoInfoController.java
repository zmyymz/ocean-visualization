package com.csu.oceanvisualization.controller;

import com.csu.oceanvisualization.commonutils.Result;
import com.csu.oceanvisualization.service.GeoJsonDataService;
import com.csu.oceanvisualization.servicebase.exceptionhandler.OceanException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("getGeojsonData/{fourOceans}/{tpSeq}")
    public Result getGeoJsonData(@PathVariable String fourOceans, @PathVariable String tpSeq) throws ExecutionException {
        String geoJsonData = geoJsonDataService.getGeoJsonData(fourOceans, tpSeq);
        return Result.ok().data("geoJsonData", geoJsonData);
    }
}
