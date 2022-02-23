package com.csu.oceanvisualization.controller;

import com.csu.oceanvisualization.commonutils.Result;
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
    @GetMapping("processData")
    public Result processData() throws ExecutionException {
        // todo 调用AbstractOcean
        return null;
        // return Result.ok().data("geoJsonData", geoJsonData);
    }
}
