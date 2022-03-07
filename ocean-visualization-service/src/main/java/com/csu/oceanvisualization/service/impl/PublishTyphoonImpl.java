package com.csu.oceanvisualization.service.impl;

import com.csu.oceanvisualization.service.AbstractTyphoon;
import com.csu.oceanvisualization.utils.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.service
 * @date 2022/3/5 14:13
 */
@Service
public class PublishTyphoonImpl extends AbstractTyphoon {
    // /home/user_typhoon_data
    @Value("${com.csu.typhoon.userfile-path}")
    private String userFilePath;

    // /geoserver/typhoon_data_temp
    @Value("${com.csu.typhoon.serverfile-path}")
    private String serverTempFilePath;

    @Value("${com.csu.typhoon.file-property-path}")
    private String serverFilePropertyPath;

    @Override
    protected void traverseFile() {
        // 递归将userFilePath下的文件复制到serverTempFilePath
    }

    @Override
    protected void countTyphoonSeq() {
        // 依次统计WP,NA,EP下的txt数量
        ConcurrentHashMap<String, List<String>> map = new ConcurrentHashMap<>();
        File typhoonFolder = new File(serverTempFilePath);
        File[] typhoonFilePath = typhoonFolder.listFiles();
        assert typhoonFilePath != null;
        for (File file : typhoonFilePath) {
            if (file.getName().contains("EP")) {
                map.put("EP", FileUtils.getTxtFilesCount(file));
            } else if (file.getName().contains("WP")) {
                map.put("WP", FileUtils.getTxtFilesCount(file));
            } else if (file.getName().contains("NA")) {
                map.put("NA", FileUtils.getTxtFilesCount(file));
            }
        }


        String metaDataJsonPath = serverFilePropertyPath + "/typhoonMetaData.json";
        File file = new File(FilenameUtils.separatorsToSystem(metaDataJsonPath));
        if (!file.exists()) {
            try {
                file.createNewFile();
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(file, map);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(file, map);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void publishTifLayer() {

    }
}
