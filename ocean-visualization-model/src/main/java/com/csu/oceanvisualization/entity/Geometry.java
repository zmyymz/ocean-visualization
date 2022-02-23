package com.csu.oceanvisualization.entity;

import lombok.Data;

import java.util.List;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.entity
 * @date 2022/2/23 15:58
 */
@Data
public class Geometry {
    public String type;
    public List<Double> coordinates;
}
