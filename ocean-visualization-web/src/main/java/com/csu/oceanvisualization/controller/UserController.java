package com.csu.oceanvisualization.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public String getUserInfo(){
        return "hello";
    }
}
