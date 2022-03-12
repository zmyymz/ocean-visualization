package com.csu.oceanvisualization.service.impl;

import com.csu.oceanvisualization.service.TyphoonMetaDataService;
import com.csu.oceanvisualization.servicebase.exceptionhandler.OceanException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.service
 * @date 2022/3/5 14:51
 */
@Service
public class TyphoonMetaDataServiceImpl implements TyphoonMetaDataService {
    @Value("${com.csu.typhoon.file-property-path}")
    private String serverFilePropertyPath;

    @Override
    public List<String> getTyphoonMetaData(String fourOceans) {
        ObjectMapper objectMapper = new ObjectMapper();
        ConcurrentHashMap<String, List<String>> concurrentHashMap = null;
        try {
            String metaDataJsonPath = serverFilePropertyPath + "/typhoonMetaData.json";
            concurrentHashMap = objectMapper.readValue(new File(FilenameUtils.separatorsToSystem(metaDataJsonPath)), ConcurrentHashMap.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new OceanException(20001, "获取台风元数据失败");
        }
        // System.out.println(concurrentHashMap);
        return concurrentHashMap.get(fourOceans.toUpperCase());
    }
}
