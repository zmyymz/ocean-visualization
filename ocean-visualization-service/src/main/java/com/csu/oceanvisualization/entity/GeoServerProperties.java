package com.csu.oceanvisualization.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.entity
 * @date 2022/3/18 10:38
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "geoserver")
public class GeoServerProperties {
    private String url;
    private String username;
    private String password;
    private String workspace;
}
