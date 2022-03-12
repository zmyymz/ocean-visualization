package com.csu.oceanvisualization.controller;

import com.csu.oceanvisualization.commonutils.Result;
import com.csu.oceanvisualization.service.AbstractOcean;
import com.csu.oceanvisualization.service.impl.PublishOceanImpl;
import com.csu.oceanvisualization.service.impl.PublishTyphoonImpl;
import org.checkerframework.checker.units.qual.A;
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
 * @date 2022/2/22 13:46
 */
@RestController
@RequestMapping("/ocean")
@CrossOrigin
public class ProcessDataController {
    @Autowired
    private PublishOceanImpl publishOcean;

    @Autowired
    private PublishTyphoonImpl publishTyphoon;

    @GetMapping("processOceanData")
    public Result processOceanData() throws ExecutionException {
        publishOcean.publishOceanLayer();
        return Result.ok();
    }

    @GetMapping("processTyphoonData")
    public Result processTyphoonData() throws ExecutionException {
        publishTyphoon.publishTyphoonLayer();;
        return Result.ok();
    }
}
