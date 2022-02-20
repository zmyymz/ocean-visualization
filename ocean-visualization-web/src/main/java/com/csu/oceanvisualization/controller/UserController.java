package com.csu.oceanvisualization.controller;

import com.csu.oceanvisualization.servicebase.exceptionhandler.OceanException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.csu.oceanvisualization.commonutils.Result;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.controller
 * @date 2022/2/19 10:54
 */
@RestController
@RequestMapping("/ocean/user")
@CrossOrigin
public class UserController {

    @GetMapping("find")
    public Result getUserInfo() {
        try {
            int a = 10 / 0;
        } catch (Exception e) {
            throw new OceanException(20001, "发生自定义异常");
        }
        return Result.ok().message("hello");
    }
}
