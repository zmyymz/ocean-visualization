package com.csu.oceanvisualization.service.impl;

import com.csu.oceanvisualization.entity.GeoServerProperties;
import com.csu.oceanvisualization.service.AbstractTyphoon;
import com.csu.oceanvisualization.servicebase.exceptionhandler.OceanException;
import com.csu.oceanvisualization.utils.FileUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.geosolutions.geoserver.rest.GeoServerRESTManager;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.manager.GeoServerRESTStyleManager;
import org.apache.commons.io.FilenameUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.service
 * @date 2022/3/5 14:13
 */
@Slf4j
@Service
public class PublishTyphoonImpl extends AbstractTyphoon {
    @Value("${com.csu.typhoon.userfile-path}")
    private String userFilePath;

    @Value("${com.csu.ocean.userstyle-path}")
    private String userStyleFilePath;

    @Value("${com.csu.typhoon.serverfile-path}")
    private String serverTempFilePath;

    @Value("${com.csu.typhoon.file-property-path}")
    private String serverFilePropertyPath;

    @Value("${com.csu.typhoon.stylefile-path}")
    private String serverStyleFilePath;

    @Autowired
    private GeoServerProperties geoServerProperties;

    @Override
    protected void traverseFile() {
        log.info("开始复制台风文件");
        log.info("Start copy typhoon data");
        System.out.println("开始复制台风文件");
        // 递归将userFilePath下的文件复制到serverTempFilePath
        File srcPath = new File(userFilePath);

        //创建目的路径对象
        File destPath = new File(serverTempFilePath);
        if (!destPath.exists()) {
            destPath.mkdir();
        }

        //获取源路径下所有文件
        File[] srcFileList = srcPath.listFiles();
        System.out.println(srcFileList);
        //遍历每一个文件
        for (File file : srcFileList) {
            System.out.println(file.getName());
            // 获取每一个文件的路径
            // System.out.println(file.getCanonicalPath());
            // File newDestPath = new File(destPath,file.getName());
            try {
                FileUtil.copyFolder(file, destPath);
                // int a = 10 / 0;
            } catch (Exception e) {
                // e.printStackTrace();
                throw new OceanException(20001, "台风数据复制出现异常");
            }
        }
    }

    @Override
    protected void copyStyleFiles() {
        log.info("开始复制样式文件");
        log.info("Start copy style file");
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

    @Override
    protected void countTyphoonSeq() {
        log.info("开始计算台风元数据");
        log.info("Start get Typhoon Metadata");
        // 依次统计WP,NA,EP下的txt数量
        ConcurrentHashMap<String, List<String>> map = new ConcurrentHashMap<>();
        File typhoonFolder = new File(serverTempFilePath);
        File[] typhoonFilePath = typhoonFolder.listFiles();
        assert typhoonFilePath != null;
        for (File file : typhoonFilePath) {
            if (file.getName().contains("EP")) {
                map.put("EP", FileUtil.getTxtFilesCount(file));
            } else if (file.getName().contains("WP")) {
                map.put("WP", FileUtil.getTxtFilesCount(file));
            } else if (file.getName().contains("NA")) {
                map.put("NA", FileUtil.getTxtFilesCount(file));
            }
        }


        String metaDataJsonPath = serverFilePropertyPath + "typhoonMetaData.json";
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

    @SneakyThrows
    @Override
    protected void publishTifLayer() {
        // 发布serverTempFilePath下的文件
        // 发布serverTempFilePath下的文件
        // String sldPath = "D:\\work\\ocean_project\\data\\datasld\\wind_style.sld";
        // String ncpath = "D:\\ocean\\user_ocean_data\\user_typhoon";
        // String ncurl = "file:/D:/ocean/user_typhoon_data";
        // String jsonFilePath = "D:\\work\\ocean_project\\json\\ww.json";

        log.info("开始发布台风图层");
        log.info("Start publish typhoon layer");
        long start = System.nanoTime();
        String sldPath = serverStyleFilePath + "wind_style.sld";
        String ncpath = serverTempFilePath;
        String ncurl = "file://" + serverTempFilePath;
        String jsonFilePath = serverFilePropertyPath + "ww.json";

        String username = geoServerProperties.getUsername();
        String password = geoServerProperties.getPassword();
        String workspace = geoServerProperties.getWorkspace();
        String sldName = "wind_style";

        addStyle(sldName, sldPath, username, password, workspace);

        String urlg = geoServerProperties.getUrl();
        createWorkspace(urlg, username, password, workspace);
        // String url = "http://localhost:8089/geoserver/rest/workspaces/" + workspace + "/coveragestores";
        String url = urlg + "/rest/workspaces/" + workspace + "/coveragestores";

        String[] folderName = FileUtil.getFileName(ncpath);//获得文件夹名称
        for (String folder : folderName) {
            System.out.println(folder);
            String[] folderSplit = folder.split("_");
            String[] ncArray = FileUtil.getFileName(ncpath + "/" + folder);
            for (String ncf : ncArray) {
                File file = new File(ncpath + "/" + folder + "/" + ncf);
                if (file.isFile()) {//如果是单个文件跳过
                    continue;
                }
                String[] ncfiles = FileUtil.getFileName(ncpath + "/" + folder + "/" + ncf);
                for (String ncfile : ncfiles) {
                    //System.out.println(ncfile);
                    if (ncfile.endsWith("pl.nc")) {
                        String ncname = ncfile.split("\\.")[0];
                        String storename = folderSplit[0] + "_" + ncf + "_" + ncname;
                        //"file:/D:/work/ocean_project/typhoon_data/typhoon_data/WP_solo/data_seq1/tp_1_pl.nc";
                        // String filename = ncurl + "/" + folder + "/" + ncf + "/" + ncfile;
                        String filename = ncurl + folder + "/" + ncf + "/" + ncfile;

                        System.out.println(filename);
                        uploadNcData(url, username, password, storename, workspace, filename);
                        String url1 = urlg + "/rest/workspaces/" + workspace + "/coveragestores/" + storename + "/coverages";
                        createView(url1, username, password, jsonFilePath, storename, workspace);
                        String url2 = urlg + "/rest/layers/" + storename + ".json";
                        String url3 = urlg + "/rest/layers/" + storename;
                        addStyleForView(url2, url3, workspace, username, password, sldName);
                    }
                }
            }
        }
        log.info("发布台风图层完成");
        log.info("Finish publish typhoon layer");
        long end = System.nanoTime();
        log.info("Finished all threads, 共耗时: " + String.valueOf(end - start) + "ns");
    }


    private void createWorkspace(String url, String username, String password, String workSpace) throws MalformedURLException {
        //url = http://localhost:8089/geoserver
        URL u = new URL(url);
        GeoServerRESTManager manager = new GeoServerRESTManager(u, username, password);
        GeoServerRESTPublisher publisher = manager.getPublisher();
        List<String> workspaces = manager.getReader().getWorkspaceNames();
        if (!workspaces.contains(workSpace)) {
            boolean createws = publisher.createWorkspace(workSpace);
            System.out.println("create workspace : " + createws);
            log.info("create workspace : " + createws);
        } else {
            System.out.println("workspace已经存在了, workSpace :" + workSpace);
            log.info("workspace already exists, workSpace :" + workSpace);
        }
    }

    public void uploadNcData(String urln, String username, String password, String storename, String workspace, String filename) {
        // storename 存储数据名称 workspace 存储工作空间 filename 本地文件地址
        // 将本地nc文件(地址filename) 存储在workplace 中命名为storename
        // "file:/D:/work/ocean_project/typhoon_data/typhoon_data/WP_solo/data_seq1/tp_1_pl.nc"
        // "http://localhost:8089/geoserver/rest/workspaces/cite/coveragestores
        // String urlg = "http://localhost:8089/geoserver";
        // createWorkspace(urlg,username,password,workspace);
        log.info("Start uploadNcData by http");
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> map1 = new HashMap<String, Object>();
        Map<String, Object> map2 = new HashMap<String, Object>();
        map1.put("name", storename);
        map1.put("type", "NetCDF");
        map1.put("enabled", "true");
        map2.put("name", workspace);
        map1.put("workspace", map2);
        map1.put("__default", "false");
        map1.put("url", filename);
        map.put("coverageStore", map1);
        JSONObject json = new JSONObject(map);

        // String username = "admin"; //用户名
        // String password = "geoserver";//密码
        URL url = null;//访问的geoserver网址
        try {
            url = new URL(urln);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();//创建连接
            String author = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            connection.setRequestProperty("Authorization", author);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.connect();
            // 发送
            OutputStream out = new DataOutputStream(connection.getOutputStream());
            // 写入请求的字符串
            // System.out.println(json.toString());
            out.write((json.toString()).getBytes("UTF-8"));
            out.flush();
            out.close();
            System.out.println("uploadNcData ResponseCode: " + connection.getResponseCode());
        } catch (IOException e) {
            // e.printStackTrace();
            throw new OceanException(20001, "uploadNcData: Network Error");
        }

    }

    public void createView(String urln, String username, String password, String jsonFilePath, String viewname, String workspace) throws IOException {
        // jsonFilePath 模板json文件在本地位置
        // viewname 图层名称
        // workspace 工作空间名称
        // String jsonFilePath = "D:\\work\\ocean_project\\json\\ww.json";//模板文件
        log.info("Configure u,v Coverage View : createView");
        String geoServerUrl = geoServerProperties.getUrl();
        File file = new File(jsonFilePath);
        String input = FileUtils.readFileToString(file, "UTF-8");
        JSONObject obj = new JSONObject(input);
        //System.out.println(obj);
        String coverage = (String) obj.optString("coverage");
        JSONObject coverageObj = JSONObject.fromString(coverage);
        coverageObj.put("name", viewname);//修改
        coverageObj.put("nativeName", viewname);
        coverageObj.put("nativeCoverageName", viewname);
        String namespace = (String) coverageObj.optString("namespace");
        JSONObject namespaceObj = JSONObject.fromString(namespace);
        namespaceObj.put("name", workspace);
        String href = geoServerUrl + "/rest/namespaces/" + workspace + ".json";
        namespaceObj.put("href", href);
        coverageObj.put("namespace", namespaceObj);
        // coverageObj.put("defaultStyle","wind_style");
        coverageObj.put("title", viewname);
        JSONArray stringArray = coverageObj.getJSONObject("keywords").getJSONArray("string");
        //System.out.println(stringArray);
        stringArray.put(0, viewname);
        coverageObj.getJSONObject("keywords").put("string", stringArray);
        JSONObject metadataEntryArray = (JSONObject) coverageObj.getJSONObject("metadata").getJSONArray("entry").get(0);
        JSONObject metadataEntryCoverage = metadataEntryArray.getJSONObject("coverageView");
        metadataEntryCoverage.put("name", viewname);
        metadataEntryArray.put("coverageView", metadataEntryCoverage);
        coverageObj.getJSONObject("metadata").getJSONArray("entry").put(0, metadataEntryArray);
        JSONObject storeObj = (JSONObject) coverageObj.getJSONObject("store");
        String storeName = workspace + ":" + viewname;
        storeObj.put("name", storeName);
        String href1 = geoServerUrl + "/rest/workspaces/" + workspace + "/coveragestores/" + viewname + ".json";
        storeObj.put("href", href1);
        coverageObj.put("store", storeObj);
        obj.put("coverage", coverageObj);
        // System.out.println(obj);
        // "http://localhost:8089/geoserver/rest/workspaces/nurc/coveragestores/netcdfstore/coverages"
        URL url = new URL(urln);//访问的geoserver网址
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String author = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        connection.setRequestProperty("Authorization", author);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.connect();
        // 发送
        OutputStream out = new DataOutputStream(connection.getOutputStream());
        // 写入请求的字符串
        System.out.println(obj.toString());
        out.write((obj.toString()).getBytes("UTF-8"));
        out.flush();
        out.close();
        System.out.println("createView ResponseCode: " + connection.getResponseCode());
        log.info("createView ResponseCode: " + connection.getResponseCode());
    }

    public String loadJson(String url, String username, String password) throws IOException {
        log.info("loadJson");
        StringBuilder json = new StringBuilder();
        URL urlObj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        String author = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        connection.setRequestProperty("Authorization", author);
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.connect();
        // 发送
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String inputLine = null;
        while ((inputLine = in.readLine()) != null) {
            json.append(inputLine);
        }
        return json.toString();
    }

    public void addStyle(String sldName, String sldPath, String username, String password, String workSpace) throws MalformedURLException {
        log.info("为台风添加样式");
        log.info("addStyle for typhoon");
        URL u = new URL(geoServerProperties.getUrl());
        GeoServerRESTManager manager = new GeoServerRESTManager(u, username, password);
        GeoServerRESTStyleManager styleManager = manager.getStyleManager();
        boolean b = styleManager.existsStyle(workSpace, sldName);
        if (!b) {
            File sldFile = new File(sldPath);
            System.out.println("addStyle: " + sldPath);
            boolean b1 = styleManager.publishStyleInWorkspace(workSpace, sldFile, sldName);
            if (!b1) {
                System.out.println("新增样式失败");
                log.warn("Adding style failed");
            }
        }
    }

    public void addStyleForView(String url1, String url2, String workspace, String username, String password, String sldName) throws IOException {
        // String url ="http://localhost:8089/geoserver/rest/layers/netcdfView.json";
        // String url2 = "http://localhost:8089/geoserver/rest/layers/netcdfView";
        log.info("addStyleForView");
        String geoServerUrl = geoServerProperties.getUrl();
        String json = loadJson(url1, username, password);
        // System.out.println(json);
        JSONObject obj = JSONObject.fromString(json);
        String layerString = (String) obj.optString("layer");
        JSONObject layerObj = JSONObject.fromString(layerString);
        String styleString = (String) layerObj.optString("defaultStyle");
        JSONObject styleObj = JSONObject.fromString(styleString);
        styleObj.put("name", sldName);
        styleObj.put("href", geoServerUrl + "/rest/styles/" + sldName + ".json");
        layerObj.put("defaultStyle", styleObj);
        obj.put("layer", layerObj);

        URL urlObj = new URL(url2);//访问的geoserver网址
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();//创建连接
        String author = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        connection.setRequestProperty("Authorization", author);
        connection.setRequestMethod("PUT");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.connect();
        // 发送
        OutputStream out = new DataOutputStream(connection.getOutputStream());
        // 写入请求的字符串
        System.out.println(obj.toString());
        out.write((obj.toString()).getBytes("UTF-8"));
        out.flush();
        out.close();
        System.out.println("addStyleForView ResponseCode: " + connection.getResponseCode());
        log.info("addStyleForView ResponseCode: " + connection.getResponseCode());
    }


}
