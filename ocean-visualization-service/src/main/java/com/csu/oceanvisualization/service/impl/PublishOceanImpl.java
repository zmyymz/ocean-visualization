package com.csu.oceanvisualization.service.impl;

import com.csu.oceanvisualization.service.AbstractOcean;
import com.csu.oceanvisualization.servicebase.exceptionhandler.OceanException;
import com.csu.oceanvisualization.utils.CMDUtils;
import com.csu.oceanvisualization.utils.FileUtils;
import com.csu.oceanvisualization.utils.GDALUtils;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.service
 * @date 2022/3/3 14:04
 */
@Service
public class PublishOceanImpl extends AbstractOcean {
    // /home/user_ocean_data
    @Value("${com.csu.ocean.userfile-path}")
    private String userFilePath;

    // /geoserver/ocean_data_temp
    @Value("${com.csu.ocean.serverfile-path}")
    private String serverTempFilePath;

    @Value("${com.csu.typhoon.file-property-path}")
    private String serverFilePropertyPath;

    // /geoserver/ocean_data
    @Value("${com.csu.ocean.servertiffile-path}")
    private String serverTifFilePath;

    @Value("${com.csu.projectPath}")
    private String projectPath;

    @Override
    protected void traverseFile() {
        // 1. 遍历userFilePath目录, 获取所有nc路径

        // 2. 先判断serverTempFilePath是否有这些文件, 根据md5

        // 3. 如果没有就将所有nc文件复制到 serverTempFilePath, 计算文件md5, 将md5值写入文件/geoserver/property/ncfilemd5

        // 4. 如果有则删除serverTempFilePath下的文件, 不再复制, 只复制新文件

        // 1. 遍历userFilePath目录, 获取所有nc路径
        File src = new File(userFilePath);
        File dest = new File(serverTempFilePath);
        if (!dest.exists()) {
            dest.mkdir();
        }
        //获取源路径下所有文件
        File[] srcFileList = src.listFiles();
        //遍历每一个文件
        for (File file : srcFileList) {
            File newDestPath = new File(dest, file.getName());
            // 2. 先判断serverTempFilePath是否有这些文件, 根据md5
            //不存在与源文件md5相同的文件,则拷贝
            if (!check(file, dest)) {
                // 4. 如果有则删除serverTempFilePath下的文件, 不再复制, 只复制新文件
                try {
                    FileUtils.copyFile(file, newDestPath);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new OceanException(20001, "复制海洋数据出现异常");
                }
            }
        }
    }

    @SneakyThrows
    @Override
    protected void calculateError() {
        // 只获取当前目录下的nc文件, 然后依次添加新的变量
        List<File> files = Files.list(Paths.get(serverTempFilePath))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".nc"))
                .map(Path::toFile)
                .collect(Collectors.toList());

        String property = System.getProperties().getProperty("os.name");

        files.forEach(file -> {
            if (property.toLowerCase().startsWith("win")) {
                String scriptRelativePath = "ocean-visualization/ocean-visualization-service/src/main/java/com/csu/oceanvisualization/scripts/addErrorVariable.py";
                String scriptPathPath = FilenameUtils.separatorsToSystem(projectPath + scriptRelativePath);
                String commandStr = "cmd /c python " + scriptPathPath + " " + file;
                // CMDUtils.executeCMD(commandStr);
                System.out.println(commandStr);
            } else {
                // 执行 linux cmd
                String commandStr = "python addErrorVariable.py " + file;
                CMDUtils.executeCMD(commandStr);
            }
        });
    }

    @Override
    protected void gdalTranslate() {
        // 遍历serverFilePath目录下的所有文件, 依次执行gdal_translate命令
        File ncFolder = new File(serverTempFilePath);
        File[] ncFilePath = ncFolder.listFiles();
        String property = System.getProperties().getProperty("os.name");

        if (ncFilePath != null) {
            for (File file : ncFilePath) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".nc")) {
                        try {
                            GDALUtils.gdalTranslate(serverTempFilePath, serverTifFilePath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void publishTifLayer() {
        // 发布serverTifFilePath (/geoserver/ocean_data) 目录下的所有tif文件

    }

    @Override
    protected void deleteTifFile() {
        // FileUtils.delDir(serverTifFilePath);
    }


    /**
     * 判断目的目录下是否有和源文件md5值相同的文件
     *
     * @param oldFile 源文件
     * @param dest    目的目录
     * @return
     */
    @SneakyThrows
    public boolean check(File oldFile, File dest) {
        String oldMd5 = FileUtils.md5(oldFile);

        String path = FilenameUtils.separatorsToSystem(serverFilePropertyPath + "ncfilemd5.txt");
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        StringBuilder result = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));//构造一个BufferedReader类来读取文件

            String s = null;
            while ((s = br.readLine()) != null) {//使用readLine方法，一次读一行
                assert oldMd5 != null;
                if (oldMd5.equals(s)) {
                    br.close();
                    return true;
                }
            }
            //
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. 如果没有就将所有nc文件复制到 serverTempFilePath,
        //计算文件md5, 写入文件/geoserver/property/ncfilemd5
        writeMd5(oldMd5);
        return false;
    }

    /**
     * 将源文件的md5写入test.txt文本中
     *
     * @param md5
     */
    public void writeMd5(String md5) {
        String filePath = FilenameUtils.separatorsToSystem(serverFilePropertyPath + "ncfilemd5.txt");

        FileWriter fwriter = null;
        try {
            //不覆盖原来的内容,直接添加到后面
            fwriter = new FileWriter(filePath, true);
            fwriter.write(md5);
            fwriter.write("\r\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
