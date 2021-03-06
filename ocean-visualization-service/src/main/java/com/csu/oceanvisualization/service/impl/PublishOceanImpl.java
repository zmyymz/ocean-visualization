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

    @Value("${com.csu.ocean.need-gdal-translate}")
    private boolean NEED_GDAL_TRANSLATE;

    @Value("${com.csu.ocean.need-calculate-error}")
    private boolean NEED_CALCULATE_ERROR;

    @Override
    protected boolean needGdalTranslate() {
        return NEED_GDAL_TRANSLATE;
    }

    @Override
    protected boolean needCalculateError() {
        return NEED_CALCULATE_ERROR;
    }

    @Override
    protected boolean needPublishTifLayer() {
        return true;
    }

    @Override
    protected void traverseFile() {
        // 1. ??????userFilePath??????, ????????????nc??????
        // 2. ?????????serverTempFilePath?????????????????????, ??????md5
        // 3. ????????????????????????nc??????????????? serverTempFilePath, ????????????md5, ???md5???????????????/geoserver/property/ncfilemd5
        // 4. ??????????????????serverTempFilePath????????????, ????????????, ??????????????????

        // 1. ??????userFilePath??????, ????????????nc??????
        log.info("PublishOceanImpl>>traverseFile() start copy ocean data");
        File src = new File(userFilePath);
        File dest = new File(serverTempFilePath);
        // System.out.println(userFilePath);
        if (!dest.exists()) {
            dest.mkdir();
        }
        // ??????????????????????????????
        File[] srcFileList = src.listFiles();
        // System.out.println(srcFileList.length);
        try {
            //?????????????????????
            assert srcFileList != null;
            for (File file : srcFileList) {
                File newDestPath = new File(dest, file.getName());
                // 2. ?????????serverTempFilePath?????????????????????, ??????md5
                // ?????????????????????md5???????????????,?????????
                if (!check(file, dest)) {
                    // 4. ??????????????????serverTempFilePath????????????, ????????????, ??????????????????
                    FileUtil.copyFile(file, newDestPath);
                    // int a = 10 / 0;
                }
            }
        } catch (Exception e) {
            log.error("PublishOceanImpl>>traverseFile() error: ", e);
            throw new OceanException(20001, "??????????????????????????????");
        }
    }

    @Override
    protected void copyStyleFiles() {
        log.info("PublishOceanImpl>>copyStyleFiles() start copy style files");
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
            log.error("PublishOceanImpl>>copyStyleFiles() error: ", e);
            throw new OceanException(20001, "??????????????????????????????");
        }
    }

    @SneakyThrows
    @Override
    protected void calculateError() {
        log.info("PublishOceanImpl>>calculateError: start add two variables");
        // ???????????????????????????nc??????, ??????????????????????????????
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
                log.info("PublishOceanImpl>>calculateError CMDUtils.executeCMD: " + commandStr);
                // System.out.println(commandStr);
            } else {
                // ?????? linux cmd
                String commandStr = "python " + scriptPathPath + " " + file;
                CMDUtils.executeCMD(commandStr);
                log.info("PublishOceanImpl>>calculateError CMDUtils.executeCMD: " + commandStr);
            }
        });
    }

    @Override
    protected void gdalTranslate() {
        // ??????serverFilePath????????????????????????, ????????????gdal_translate??????
        log.info("?????????nc??????tif");
        log.info("PublishOceanImpl>>gdalTranslate Start nc -> tif");
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
                            log.error("PublishOceanImpl>>gdalTranslate error: ", e);
                            throw new OceanException(20001, "GDALUtils.gdalTranslate??????");
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void publishTifLayer() {
        // ??????serverTifFilePath (/geoserver/ocean_data) ??????????????????tif??????
        // ??????serverTifFilePath (/geoserver/ocean_data) ??????????????????tif??????
        // String tifpath = "D:\\work\\ocean_project\\Ocean\\user_ocean_data";//????????????
        // String sldPath = "D:\\work\\ocean_project\\data\\datasld";
        log.info("????????????tif??????");
        log.info("PublishOceanImpl>>publishTifLayer Start publish tif layer");
        String tifpath = serverTifFilePath;
        String sldPath = serverStyleFilePath;
        String url = geoServerProperties.getUrl();
        String username = geoServerProperties.getUsername();
        String password = geoServerProperties.getPassword();
        String workSpace = geoServerProperties.getWorkspace();
        try {
            createWorkspace(url, username, password, workSpace);
        } catch (MalformedURLException e) {
            log.error("PublishOceanImpl>>publishTifLayer error", e);
            throw new OceanException(20001, "Geoserver????????????????????????");
        }
        String[] tifArray = FileUtil.getFileName(tifpath);//??????tif??????

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
                    String sldpath = sldPath + sldName + ".sld";
                    String tiffpath = tifpath + tif;
                    System.out.println(sldpath);
                    System.out.println(tiffpath);
                    String dataStore = tifName[0];
                    String layerStore = tifName[0];
                    log.info("?????????????????????tif??????");
                    log.info("PublishOceanImpl>>publishTifLayer Start publish tif layer with style");
                    PublishSldTiffData(url, username, password, workSpace, sldpath, sldName, tiffpath, dataStore, layerStore);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        long end = System.nanoTime();
        log.info("PublishOceanImpl>>publishTifLayer Finished all threads, cost: " + String.valueOf(end - start) + "ns");
    }

    /**
     * ??????????????????Tiff
     */
    private void PublishSldTiffData(String url, String username, String password, String workSpace, String sldPath, String sldName, String tiffPath, String dataStore, String layerStore) {
        // ???????????????????????? ??????url ???????????????workspace??? ?????????????????????sldName???tiff??????
        // url ?????????url  username ????????? password ?????? workSpace ???????????????????????????
        // sldPath ?????????????????? sldName???????????? tiffPath tiff????????????
        // dataStore ?????????????????? layerStore ??????????????????
        // String workSpace = "myFirstWorkspaceOfTif";// ??????????????????????????????????????????workspace
        try {
            URL u = new URL(url);
            GeoServerRESTManager manager = new GeoServerRESTManager(u, username, password);
            GeoServerRESTPublisher publisher = manager.getPublisher();

            // ??????????????????workspace????????????????????????????????????
            // createWorkspace(url, username, password, workSpace);
            // ??????style???????????? ????????????????????????
            addStyle(sldName, sldPath, username, password, workSpace);

            // dataStore ?????????????????? layerStore ?????????????????? tiffPath tiff????????????
            RESTDataStore restStore = manager.getReader().getDatastore(workSpace, dataStore);
            if (restStore == null) {
                GSGeoTIFFDatastoreEncoder gsGeoTIFFDatastoreEncoder = new GSGeoTIFFDatastoreEncoder(dataStore);
                gsGeoTIFFDatastoreEncoder.setWorkspaceName(workSpace);
                gsGeoTIFFDatastoreEncoder.setUrl(new URL("file:" + tiffPath));
                boolean createStore = manager.getStoreManager().create(workSpace, gsGeoTIFFDatastoreEncoder);
                if (!createStore) {
                    log.info("PublishOceanImpl>>PublishSldTiffData create store (TIFF??????????????????) : " + createStore);
                    // System.out.println("create store (TIFF??????????????????) : " + createStore);
                }
                boolean publish = publisher.publishGeoTIFF(workSpace, dataStore, layerStore, new File(tiffPath), "EPSG:4326",
                        GSResourceEncoder.ProjectionPolicy.FORCE_DECLARED, sldName, null);
                if (!publish) {
                    log.info("PublishOceanImpl>>PublishSldTiffData publish (TIFF??????????????????) : " + publish);
                    // System.out.println("publish (TIFF??????????????????) : " + publish);
                }
            } else {
                log.info("PublishOceanImpl>>PublishSldTiffData dataStore already exists, store:" + dataStore);
                // System.out.println("???????????????????????????,store:" + dataStore);
            }
        } catch (IOException e) {
            log.error("PublishOceanImpl>>PublishSldTiffData error: ", e);
            throw new OceanException(20001, "PublishSldTiffData: Network Error");
        }
    }


    /**
     * ??????geoserver????????????
     *
     * @param url
     * @param username
     * @param password
     * @param workSpace
     * @throws MalformedURLException
     */
    private void createWorkspace(String url, String username, String password, String workSpace) throws MalformedURLException {
        URL u = new URL(url);
        GeoServerRESTManager manager = new GeoServerRESTManager(u, username, password);
        GeoServerRESTPublisher publisher = manager.getPublisher();
        List<String> workspaces = manager.getReader().getWorkspaceNames();
        if (workspaces.contains(workSpace)) {
            // ??????????????????
            publisher.removeWorkspace(workSpace, true);
            log.info("PublishOceanImpl>>createWorkspace workspace already exists, delete workSpace :" + workSpace);
        }
        boolean createws = publisher.createWorkspace(workSpace);
        log.info("PublishOceanImpl>>createWorkspace create workSpace : " + createws);

        // URL u = new URL(url);
        // GeoServerRESTManager manager = new GeoServerRESTManager(u, username, password);
        // GeoServerRESTPublisher publisher = manager.getPublisher();
        // List<String> workspaces = manager.getReader().getWorkspaceNames();
        // if (!workspaces.contains(workSpace)) {
        //     boolean createws = publisher.createWorkspace(workSpace);
        //     System.out.println("create ws : " + createws);
        // } else {
        //     System.out.println("workspace???????????????, workSpace :" + workSpace);
        // }
    }


    /**
     * ?????????????????????
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
                log.warn("PublishOceanImpl>>addStyle addStyle failed");
                // System.out.println("??????????????????");
                // throw new OceanException(20001, "??????????????????");
            }
        }
    }

    @Override
    protected void deleteTifFile() {
        // FileUtils.delDir(serverTifFilePath);
    }


    /**
     * ??????????????????????????????????????????md5??????????????????
     *
     * @param oldFile ?????????
     * @param dest    ????????????
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
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));//????????????BufferedReader??????????????????
            String s = null;
            while ((s = br.readLine()) != null) {//??????readLine????????????????????????
                assert oldMd5 != null;
                if (oldMd5.equals(s)) {
                    br.close();
                    return true;
                }
            }
            br.close();
        } catch (Exception e) {
            log.error("PublishOceanImpl>>check md5 error: ", e);
            throw new OceanException(20001, "??????md5??????????????????");
        }
        // 3. ????????????????????????nc??????????????? serverTempFilePath,
        //????????????md5, ????????????/geoserver/property/ncfilemd5
        writeMd5(oldMd5);
        return false;
    }

    /**
     * ???????????????md5??????test.txt?????????
     *
     * @param md5
     */
    public void writeMd5(String md5) {
        String filePath = FilenameUtils.separatorsToSystem(serverFilePropertyPath + "ncfilemd5.txt");

        FileWriter fwriter = null;
        try {
            //????????????????????????,?????????????????????
            fwriter = new FileWriter(filePath, true);
            fwriter.write(md5);
            fwriter.write("\r\n");
        } catch (IOException ex) {
            // ex.printStackTrace();
            log.error("PublishOceanImpl>>writeMd5 error: ", ex);
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
