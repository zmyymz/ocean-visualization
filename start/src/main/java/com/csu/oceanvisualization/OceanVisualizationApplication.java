package com.csu.oceanvisualization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.oas.annotations.EnableOpenApi;

import java.util.TimeZone;

@SpringBootApplication
@EnableOpenApi
@ComponentScan(basePackages = {"com.csu"})
public class OceanVisualizationApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        SpringApplication.run(OceanVisualizationApplication.class, args);
    }

}
