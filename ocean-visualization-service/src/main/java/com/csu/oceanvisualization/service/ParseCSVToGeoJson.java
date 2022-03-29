package com.csu.oceanvisualization.service;

import org.springframework.stereotype.Service;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.service
 * @date 2022/2/23 19:49
 */
@Service
public interface ParseCSVToGeoJson {
    String parse(String fourOceans, String tpSeq);
}
