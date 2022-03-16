package com.csu.oceanvisualization.service.impl;

import com.csu.oceanvisualization.service.AbstractTyphoon;
import com.csu.oceanvisualization.servicebase.exceptionhandler.OceanException;
import com.csu.oceanvisualization.utils.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
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
    @Value("${com.csu.typhoon.userfile-path}")
    private String userFilePath;

    @Value("${com.csu.typhoon.serverfile-path}")
    private String serverTempFilePath;

    @Value("${com.csu.typhoon.file-property-path}")
    private String serverFilePropertyPath;

    @Value("${com.csu.typhoon.stylefile-path}")
    private String serverStyleFilePath;

    @Override
    protected void traverseFile() {
        // 递归将userFilePath下的文件复制到serverTempFilePath
        File srcPath = new File(userFilePath);

        //创建目的路径对象
        File destPath = new File(serverTempFilePath);
        if (!destPath.exists()) {
            destPath.mkdir();
        }

        //获取源路径下所有文件
        File[] srcFileList = srcPath.listFiles();
        //遍历每一个文件
        for (File file : srcFileList) {
            //获取每一个文件的路径
            //System.out.println(file.getCanonicalPath());

            //File newDestPath = new File(destPath,file.getName());
            try {
                copyFolder(file, destPath);
            } catch (Exception e) {
                // e.printStackTrace();
                throw new OceanException(20001, "台风数据复制出现异常");
            }
        }
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
        } else {
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
        // 发布serverTempFilePath下的文件
    }


    /**
     * 拷贝文件夹
     *
     * @param srcPath  源路径
     * @param destPath 目的路径
     * @throws Exception
     */
    public static void copyFolder(File srcPath, File destPath) throws Exception {
        //判断是否是目录
        //若是目录,则递归
        if (srcPath.isDirectory()) {
            //获取源路径下某个目录的名称
            String srcPathName = srcPath.getName();
            //在目的路径下创建新的目录
            File newDestPath = new File(destPath, srcPathName);
            //判断目的路径下该目录是否已经被创建
            if (!newDestPath.exists()) {
                newDestPath.mkdir();
                //获取源路径下所有的目录及文件
                File[] allPathList = srcPath.listFiles();
                for (File file : allPathList) {
                    //进行递归调用
                    copyFolder(file, newDestPath);
                }
            }
        }
        //若是文件则直接拷贝
        else {
            File newDestPath = new File(destPath, srcPath.getName());
            FileUtils.copyFile(srcPath, newDestPath);
        }
    }


}
