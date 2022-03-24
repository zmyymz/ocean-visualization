package com.csu.oceanvisualization.service.impl;

import com.csu.oceanvisualization.entity.GeoServerProperties;
import com.csu.oceanvisualization.service.AbstractOcean;
import com.csu.oceanvisualization.servicebase.exceptionhandler.OceanException;
import com.csu.oceanvisualization.utils.CMDUtils;
import com.csu.oceanvisualization.utils.FileUtil;
import com.csu.oceanvisualization.utils.GDALUtils;
import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder;
import it.geosolutions.geoserver.rest.manager.GeoServerRESTStyleManager;
import it.geosolutions.geoserver.rest.encoder.datastore.GSGeoTIFFDatastoreEncoder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.service
 * @date 2022/3/3 14:04
 */
@Slf4j
@Service
public class PublishOceanImpl extends AbstractOcean {
    @Value("${com.csu.ocean.userfile-path}")
    private String userFilePath;

    @Value("${com.csu.ocean.serverfile-path}")
    private String serverTempFilePath;

    @Value("${com.csu.typhoon.file-property-path}")
    private String serverFilePropertyPath;

    @Value("${com.csu.ocean.servertiffile-path}")
    private String serverTifFilePath;

    @Value("${com.csu.projectPath}")
    private String projectPath;

    @Value("${com.csu.ocean.userstyle-path}")
    private String userStyleFilePath;

    @Value("${com.csu.typhoon.stylefile-path}")
    private String serverStyleFilePath;

    @Autowired
    private GeoServerProperties geoServerProperties;

    @Override
    protected boolean needGdalTranslate() {
        return false;
    }

    @Override
    protected boolean needCalculateError() {
        return false;
    }

    @Override
    protected boolean needPublishTifLayer() {
        return true;
    }

    @Override
    protected void traverseFile() {
        // 1. 遍历userFilePath目录, 获取所有nc路径
        // 2. 先判断serverTempFilePath是否有这些文件, 根据md5
        // 3. 如果没有就将所有nc文件复制到 serverTempFilePath, 计算文件md5, 将md5值写入文件/geoserver/property/ncfilemd5
        // 4. 如果有则删除serverTempFilePath下的文件, 不再复制, 只复制新文件

        // 1. 遍历userFilePath目录, 获取所有nc路径
        log.info("开始复制海洋数据");
        File src = new File(userFilePath);
        File dest = new File(serverTempFilePath);
        if (!dest.exists()) {
            dest.mkdir();
        }
        // 获取源路径下所有文件
        File[] srcFileList = src.listFiles();
        try {
            //遍历每一个文件
            assert srcFileList != null;
            for (File file : srcFileList) {
                File newDestPath = new File(dest, file.getName());
                // 2. 先判断serverTempFilePath是否有这些文件, 根据md5
                // 不存在与源文件md5相同的文件,则拷贝
                if (!check(file, dest)) {
                    // 4. 如果有则删除serverTempFilePath下的文件, 不再复制, 只复制新文件
                    FileUtil.copyFile(file, newDestPath);
                    // int a = 10 / 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new OceanException(20001, "复制海洋数据出现异常");
        }
    }

    @Override
    protected void copyStyleFiles() {
        log.info("开始复制样式文件");
        File srcPath = new File(userStyleFilePath);
        File destPath = new File(serverStyleFilePath);
        if (!destPath.exists()) {
            destPath.mkdir();
        }
        File[] srcFileList = srcPath.listFiles();
        try {
            assert srcFileList != null;
            for (File file : srcFileList) {
                FileUtil.copyFolder(file, destPath);
            }
        } catch (Exception e) {
            throw new OceanException(20001, "样式文件复制出现异常");
        }
    }

    @SneakyThrows
    @Override
    protected void calculateError() {
        log.info("正在为数据添加两个变量");
        // 只获取当前目录下的nc文件, 然后依次添加新的变量
        List<File> files = Files.list(Paths.get(serverTempFilePath))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".nc"))
                .map(Path::toFile)
                .collect(Collectors.toList());

        String property = System.getProperties().getProperty("os.name");
        String scriptRelativePath = "ocean-visualization/ocean-visualization-service/src/main/java/com/csu/oceanvisualization/scripts/addErrorVariable.py";
        String scriptPathPath = FilenameUtils.separatorsToSystem(projectPath + scriptRelativePath);

        files.forEach(file -> {
            if (property.toLowerCase().startsWith("win")) {
                String commandStr = "cmd /c python " + scriptPathPath + " " + file;
                CMDUtils.executeCMD(commandStr);
                log.info("执行添加变量脚本: " + commandStr);
                // System.out.println(commandStr);
            } else {
                // 执行 linux cmd
                String commandStr = "python " + scriptPathPath + " " + file;
                CMDUtils.executeCMD(commandStr);
                log.info("执行添加变量脚本: " + commandStr);
            }
        });
    }

    @Override
    protected void gdalTranslate() {
        // 遍历serverFilePath目录下的所有文件, 依次执行gdal_translate命令
        log.info("开始将nc转为tif");
        File ncFolder = new File(serverTempFilePath);
        File[] ncFilePath = ncFolder.listFiles();
        String property = System.getProperties().getProperty("os.name");

        if (ncFilePath != null) {
            for (File file : ncFilePath) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".nc")) {
                        try {
                            GDALUtils.gdalTranslate(FilenameUtils.separatorsToSystem(file.getAbsolutePath()), serverTifFilePath);
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
        // 发布serverTifFilePath (/geoserver/ocean_data) 目录下的所有tif文件
        // String tifpath = "D:\\work\\ocean_project\\Ocean\\user_ocean_data";//文件路径
        // String sldPath = "D:\\work\\ocean_project\\data\\datasld";
        log.info("开始发布tif图层");
        String tifpath = serverTifFilePath;
        String sldPath = serverStyleFilePath;
        String url = geoServerProperties.getUrl();
        String username = geoServerProperties.getUsername();
        String password = geoServerProperties.getPassword();
        String workSpace = geoServerProperties.getWorkspace();
        try {
            createWorkspace(url, username, password, workSpace);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new OceanException(20001, "Geoserver创建工作空间异常");
        }
        String[] tifArray = FileUtil.getFileName(tifpath);//所有tif文件

        long start = System.nanoTime();
        int processorsNum = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(processorsNum, processorsNum * 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), new ThreadPoolExecutor.AbortPolicy());
        for (String tif : tifArray) {
            executor.execute(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    String[] tifName = tif.split("\\.");
                    String[] tifAttr = tifName[0].split("_");
                    String sldName = "";
                    if (tifAttr[0].equals("SWH")) {
                        sldName = "SWH";
                    } else if (tifAttr[0].equals("SSH")) {
                        if (tifAttr[2].equals("After")) {
                            sldName = "SSH_Error_After";
                        } else if (tifAttr[2].equals("Before")) {
                            sldName = "SSH_Error_Before";
                        } else {
                            sldName = "SSH";
                        }
                    } else if (tifAttr[0].contains("wave")) {
                        if (tifAttr[2].contains("forecast")) {
                            sldName = "wave_direction_reanalysis_forecast";
                        } else if (tifAttr[2].contains("predict")) {
                            sldName = "wave_direction_reanalysis_predict";
                        } else {
                            sldName = "wave_direction";
                        }
                    } else {
                        if (tifAttr[2].equals("After")) {
                            sldName = "temp_Error_After";
                        } else if (tifAttr[2].equals("Before")) {
                            sldName = "temp_Error_Before";
                        } else {
                            sldName = "temperature";
                        }
                    }
                    String sldpath = sldPath + "\\" + sldName + ".sld";
                    String tiffpath = tifpath + "\\" + tif;
                    String dataStore = tifName[0];
                    String layerStore = tifName[0];
                    log.info("发布带有样式的tif图层");
                    PublishSldTiffData(url, username, password, workSpace, sldpath, sldName, tiffpath, dataStore, layerStore);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        long end = System.nanoTime();
        log.info("Finished all threads, 共耗时: " + String.valueOf(end - start));
    }

    /**
     * 发布带样式的Tiff
     */
    private void PublishSldTiffData(String url, String username, String password, String workSpace, String sldPath, String sldName, String tiffPath, String dataStore, String layerStore) {
        // 使用用户账号密码 登录url 在工作空间workspace中 添加一个样式为sldName的tiff数据
        // url 访问的url  username 用户名 password 密码 workSpace 数据发布的工作空间
        // sldPath 样式所在路径 sldName样式名称 tiffPath tiff文件路径
        // dataStore 数据存储名称 layerStore 图层存储名称
        // String workSpace = "myFirstWorkspaceOfTif";// 待创建和发布图层的工作区名称workspace
        try {
            URL u = new URL(url);
            GeoServerRESTManager manager = new GeoServerRESTManager(u, username, password);
            GeoServerRESTPublisher publisher = manager.getPublisher();

            // 判断工作区（workspace）是否存在，不存在则创建
            // createWorkspace(url, username, password, workSpace);
            // 判断style是否存在 若不存在需要创建
            addStyle(sldName, sldPath, username, password, workSpace);

            // dataStore 数据存储名称 layerStore 图层存储名称 tiffPath tiff文件路径
            RESTDataStore restStore = manager.getReader().getDatastore(workSpace, dataStore);
            if (restStore == null) {
                GSGeoTIFFDatastoreEncoder gsGeoTIFFDatastoreEncoder = new GSGeoTIFFDatastoreEncoder(dataStore);
                gsGeoTIFFDatastoreEncoder.setWorkspaceName(workSpace);
                gsGeoTIFFDatastoreEncoder.setUrl(new URL("file:" + tiffPath));
                boolean createStore = manager.getStoreManager().create(workSpace, gsGeoTIFFDatastoreEncoder);
                if (!createStore) {
                    log.info("create store (TIFF文件创建状态) : " + createStore);
                    // System.out.println("create store (TIFF文件创建状态) : " + createStore);
                }
                boolean publish = publisher.publishGeoTIFF(workSpace, dataStore, layerStore, new File(tiffPath), "EPSG:4326",
                        GSResourceEncoder.ProjectionPolicy.FORCE_DECLARED, sldName, null);
                if (!publish) {
                    log.info("publish (TIFF文件发布状态) : " + publish);
                    // System.out.println("publish (TIFF文件发布状态) : " + publish);
                }
            } else {
                log.info("数据存储已经存在了,store:" + dataStore);
                // System.out.println("数据存储已经存在了,store:" + dataStore);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 创建geoserver工作空间
     *
     * @param url
     * @param username
     * @param password
     * @param workSpace
     * @throws MalformedURLException
     */
    private void createWorkspace(String url, String username, String password, String workSpace) throws MalformedURLException {
        // url = http://localhost:8089/geoserver
        URL u = new URL(url);
        GeoServerRESTManager manager = new GeoServerRESTManager(u, username, password);
        GeoServerRESTPublisher publisher = manager.getPublisher();
        List<String> workspaces = manager.getReader().getWorkspaceNames();
        if (!workspaces.contains(workSpace)) {
            boolean createws = publisher.createWorkspace(workSpace);
            System.out.println("create ws : " + createws);
        } else {
            System.out.println("workspace已经存在了, workSpace :" + workSpace);
        }
    }


    /**
     * 为图层添加样式
     *
     * @param sldName
     * @param sldPath
     * @param username
     * @param password
     * @param workSpace
     * @throws MalformedURLException
     */
    public void addStyle(String sldName, String sldPath, String username, String password, String workSpace) throws MalformedURLException {
        URL u = new URL(geoServerProperties.getUrl());
        GeoServerRESTManager manager = new GeoServerRESTManager(u, username, password);
        GeoServerRESTStyleManager styleManager = manager.getStyleManager();
        boolean b = styleManager.existsStyle(workSpace, sldName);
        if (!b) {
            File sldFile = new File(sldPath);
            boolean b1 = styleManager.publishStyleInWorkspace(workSpace, sldFile, sldName);
            if (!b1) {
                log.warn("新增样式失败");
                // System.out.println("新增样式失败");
                // throw new OceanException(20001, "新增样式失败");
            }
        }
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
        String oldMd5 = FileUtil.md5(oldFile);

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
