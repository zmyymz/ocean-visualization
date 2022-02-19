package com.csu.oceanvisualization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.csu"})
public class OceanVisualizationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OceanVisualizationApplication.class, args);
    }

}
