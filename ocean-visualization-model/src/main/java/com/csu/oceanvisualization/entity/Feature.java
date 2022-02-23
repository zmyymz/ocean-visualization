package com.csu.oceanvisualization.entity;

import lombok.Data;

import java.util.Properties;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.entity
 * @date 2022/2/23 15:56
 */
@Data
public class Feature {
    public String id;
    public String type;
    public TyphoonProperty properties;
    public Geometry geometry;
}
