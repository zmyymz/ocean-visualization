package com.csu.oceanvisualization.controller;

import com.csu.oceanvisualization.commonutils.Result;
import com.csu.oceanvisualization.service.TyphoonMetaDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.controller
 * @date 2022/3/5 14:04
 */
@RestController
@RequestMapping("/typhoon")
@CrossOrigin
public class TyphoonMetaDataController {
    @Autowired
    private TyphoonMetaDataService typhoonMetaDataService;

    @GetMapping("/getMetaInfo/{fourOceans}")
    public Result getMetaInfo(@PathVariable String fourOceans) throws ExecutionException {
        // 返回WP_solo下的tp_seq.txt列表
        List<String> typhoonMetaData = typhoonMetaDataService.getTyphoonMetaData(fourOceans);
        return Result.ok().data("typhoonMetaData", typhoonMetaData);
    }
}
