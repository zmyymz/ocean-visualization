package com.csu.oceanvisualization.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.entity
 * @date 2022/2/23 15:57
 */
@Data
public class TyphoonProperty {
    public String date;
    public Double latitude;
    public Double longitude;
    @JsonProperty("max wind speed(intensity)")
    public Integer maxWindSpeed;
    @JsonProperty("min pressure")
    public Integer minPressure;
    public String time;
}
