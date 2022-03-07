package com.csu.oceanvisualization;

import com.csu.oceanvisualization.entity.Feature;
import com.csu.oceanvisualization.entity.GeoJsonFeature;
import com.csu.oceanvisualization.entity.Geometry;
import com.csu.oceanvisualization.entity.TyphoonProperty;
import com.csu.oceanvisualization.utils.CMDUtils;
import com.csu.oceanvisualization.utils.DateUtils;
import com.csu.oceanvisualization.utils.FileUtils;
import com.csu.oceanvisualization.utils.GDALUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.assertj.core.util.Lists;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization
 * @date 2022/1/28 14:46
 */
// @SpringBootTest
public class OceanTest {
    @Test
    public void test01() throws IOException {
        // 1° 设置原始文件路径+转存文件路径
        String filePath = "D:/OceanVisualization/data/SSH_202104.nc";
        // String savePath = "/Users/caowei/workspace/test.nc";
        // 2° 读取文件
        // NetcdfFile netcdfFile = NetcdfFiles.open(filePath);
        NetcdfFile ncFile = NetcdfFile.open(filePath, null);

        // 获取所有的变量名
        ImmutableList<Variable> variablesList = ncFile.getVariables();
        ImmutableList<Variable> variablesShortNameList;
        ArrayList<String> variablesNameList = new ArrayList<>();
        for (Variable variable : variablesList) {
            String shortName = variable.getShortName();
            // System.out.println(shortName);
            if (shortName.equals("lat") || shortName.equals("lon") || shortName.equals("time"))
                continue;
            String variableTempName = shortName;
            variablesNameList.add(shortName);
        }

        // 设置时间变量
        Variable timeObject = ncFile.findVariable("time");
        // 读取真实时间值 59310.0 59311.0 59312.0 59313.0 59314.0
        Array read = timeObject.read();
        long timeSize = read.getSize();
        String[] period = read.toString().split(" ");
        ArrayList<Integer> timeList = new ArrayList<>();
        for (String s : period) {
            // [59310, 59311, 59312, 59313, 59314]
            timeList.add(Double.valueOf(s).intValue());
        }
        String timeAt = timeObject.getUnitsString();
        String regex = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{1}.\\d{1}";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(timeAt);
        if (matcher.find()) {
            timeAt = matcher.group(0);
        }
        // timeAt 1858-11-17 00:00:0.0
        String[] timeArray = timeAt.split(" ");
        // 日期: 1858-11-17
        String curDate = timeArray[0];
        // 时间: 00:00:0.0
        String curTime = timeArray[1];
        System.out.println(curDate);
        System.out.println(curTime);

        ArrayList<String> timestampList = new ArrayList<>();
        for (Integer time : timeList) {
            String nextDate = DateUtils.getNextDate(timeAt, time, Calendar.DATE, "yyyy-MM-dd HH:mm:ss");
            long stringToDate = DateUtils.getStringToDate(nextDate);
            timestampList.add(String.valueOf(stringToDate));
        }

        for (int i = 0; i < timeSize; i++) {
            for (String variable : variablesNameList) {
                String commandStr = "gdal_translate -projwin_srs epsg:4326 -sds -b " + (i + 1) + " NETCDF:\"" + filePath + "\":" + variable + " " + variable + "_" + timestampList.get(i) + ".tif";
                System.out.println(commandStr);
            }
        }
    }


    @Test
    public void test03() throws ParseException {
        // wave_direction时间测试
        List<Integer> timeRangeList = Arrays.asList(1054176, 1054177, 1054178, 1054179, 1054180, 1054181, 1054182);
        ArrayList<String> timeResultList = Lists.newArrayList();
        // HashSet<String> set = new HashSet<>();
        for (Integer time : timeRangeList) {
            String nextDate = DateUtils.getNextDate("1900-01-01", time, Calendar.HOUR_OF_DAY, "yyyy-MM-dd");
            timeResultList.add(nextDate);
            // set.add(nextDate);
        }
        System.out.println(timeResultList);
        // System.out.println(set);


        // 台风数据测试
        // String nextDate = getNextDate("1900-01-01 00:00:0.0", 692520, Calendar.HOUR_OF_DAY, "yyyy-MM-dd HH:mm:ss");
        // System.out.println(nextDate);


        // System.out.println(getStringToDate("1900-01-01 00:00:0.0"));
        //
        // String dateToString = getDateToString(-2209017600000L);
        // System.out.println(dateToString);


        // wave_direction
        // for (Integer time : timeRangeList) {
        //     String nextDate = getNextDate("1900-01-01 00:00:0.0", time, Calendar.HOUR_OF_DAY, "yyyy-MM-dd HH:mm:ss");
        //     timeResultList.add(nextDate);
        //     set.add(nextDate);
        // }
        // System.out.println(timeResultList);
        // System.out.println(set);


        // String curDate = getNextDate("1858-11-17", 59310, Calendar.DATE, "yyyy-MM-dd");
        // 0000-01-01   738405
        // String curDate = getNextDate("0000-01-01", 738405, Calendar.DATE, "yyyy-MM-dd");

        // System.out.println(curDate);


        // String str1 = "2013-08-11";
        // String str2 = "2013-08-15";
        // SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // Date d1 = simpleDateFormat.parse(str1);
        // Date d2 = simpleDateFormat.parse(str2);
        // int day = daysOfTwo(d1, d2);
        // System.out.println(day);
    }

    /**
     * 时间互转测试
     */
    @Test
    public void testTimeStamps() {
        System.out.println(DateUtils.getDateToString(1586059200000L));
    }

    /**
     * 执行简单cmd命令
     *
     * @param command
     */
    public String executeCMD(String command) {
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(command);
            // Process p = new ProcessBuilder(command).start();
            // ProcessBuilder pb = new ProcessBuilder(command);
            // pb.redirectErrorStream(true);
            // Process p = pb.start();
            br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            String s = br.readLine();
            // System.out.println(s);
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            // System.out.println(sb.toString());
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Test
    public void testgdalTranslate() throws IOException {
        GDALUtils.gdalTranslate("D:/OceanVisualization/data/SWH.nc", "D:/test/");
        // GDALUtils.gdalTranslate("D:/OceanVisualization/data/temp.nc", "D:/temp/");
        // GDALUtils.gdalTranslate("D:/OceanVisualization/data/wave_direction.nc", "D:/wave_direction/");

        // gdalTranslate("D:/OceanVisualization/data/wave_direction.nc", "D:/");
        // System.out.println(getDateToString(88881984000000L));
    }

    /**
     * 测试简单命令行
     */
    @Test
    public void testCommand() {
        // 拼接gdal命令
        // String commandStr = "ping www.baidu.com";
        String commandStr = "cmd /c gdal_translate -projwin_srs epsg:4326 -b 2 NETCDF:\"D:/SWH.nc\":SWH_Real D:/SWH_22222_Real.tif";
        String s = executeCMD(commandStr);
        System.out.println(s);
    }

    @Test
    public void testPythonCommand() {
        // 拼接python命令
        // String property = System.getProperties().getProperty("os.name");
        // if (property.toLowerCase().startsWith("win")){
        //     // 执行 windows cmd
        //     // String commandStr = "cmd /c ping www.baidu.com";
        //     // "I:/software/Anaconda3/python.exe"
        //     String commandStr = "cmd /c I:/software/Anaconda3/python.exe D:\\Java\\JavaEE\\IdeaProjects\\ocean-visualization\\ocean-visualization-service\\src\\main\\java\\com\\csu\\oceanvisualization\\scripts\\txt2geojson.py D:/OceanVisualization/data/typhoon_data/WP_solo/tp_seq1.txt D:/out.geojson";
        //     String result = executeCMD(commandStr);
        //     System.out.println(result);
        // }else{
        //     // 执行 linux cmd
        //     System.out.println("linux");
        // }

        String commandStr = "cmd /c python D:/Java/JavaEE/IdeaProjects/ocean-visualization/ocean-visualization-service/src/main/java/com/csu/oceanvisualization/scripts/txt2geojson.py D:/OceanVisualization/data/typhoon_data/WP_solo/tp_seq1.txt D:/out.geojson";
        String s = CMDUtils.executeCMD(commandStr);
        System.out.println(s);

        // String commandStr = "cmd /c python.exe D:/Java/JavaEE/IdeaProjects/ocean-visualization/ocean-visualization-service/src/main/java/com/csu/oceanvisualization/scripts/hello.py";
        // String s = CMDUtils.executeCMD(commandStr);
        // System.out.println(s);

    }


    @Test
    public void testCopyFile() {
        ThreadLocalRandom t = ThreadLocalRandom.current();

        System.out.println(t.nextInt(50));//随机生成0~50的随机数，不包括50

        System.out.println(t.nextInt(30, 50));//随机生成30~50的随机数，不包括50
    }

    @Test
    public void pasreCSVtoGeoJson() throws IOException {
        String csvFilePath = "D:\\OceanVisualization\\data\\typhoon_data\\WP_solo\\tp_seq1.txt";
        ArrayList<Feature> featureArrayList = new ArrayList<>();

        try (BufferedReader in = new BufferedReader(new FileReader(csvFilePath));) {
            // processing code here
            String s = null;
            StringBuilder sb = new StringBuilder();
            int id = 0;
            while ((s = in.readLine()) != null) {
                String[] line = s.split(" ");
                Double longitude = Double.valueOf(line[2]);
                Double latitude = Double.valueOf(line[3]);
                Integer minPressure = Integer.valueOf(line[4]);
                Integer maxWindSpeed = Integer.valueOf(line[5]);
                Geometry geometry = new Geometry();
                geometry.setType("Point");
                geometry.setCoordinates(Arrays.asList(longitude, latitude));


                Feature feature = new Feature();
                feature.setId(String.valueOf(id));
                id++;
                feature.setType("Feature");
                feature.setGeometry(geometry);


                TyphoonProperty properties = new TyphoonProperty();
                properties.setDate(line[0]);
                properties.setLatitude(latitude);
                properties.setLongitude(longitude);
                properties.setMaxWindSpeed(maxWindSpeed);
                properties.setMinPressure(minPressure);
                properties.setTime(line[1]);


                feature.setProperties(properties);
                featureArrayList.add(feature);
            }
        }
        GeoJsonFeature geoJsonFeature = new GeoJsonFeature();
        geoJsonFeature.setType("FeatureCollection");
        geoJsonFeature.setFeatureList(featureArrayList);
        // System.out.println(geoJsonFeature);

        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(geoJsonFeature);
        System.out.println(s);
    }

    @Test
    public void uploadNetcdf() throws IOException {
        //转换为json字符串
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> map1 = new HashMap<String, Object>();
        Map<String, Object> map2 = new HashMap<String, Object>();
        map1.put("name", "netcdfstore1");
        map1.put("type", "NetCDF");
        map1.put("enabled", "true");
        map2.put("name", "cite");
        map1.put("workspace", map2);
        map1.put("__default", "false");
        map1.put("url", "file://D://OceanVisualization//data//typhoon_data//WP_solo//data_seq1//tp_1_pl.nc");
        map.put("coverageStore", map1);

        JSONObject json = new JSONObject(map);

        String username = "admin"; //用户名
        String password = "geoserver";//密码
        URL url = new URL("http://localhost:8089/geoserver/rest/workspaces/cite/coveragestores");//访问的geoserver网址
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();//创建连接
        String author = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        connection.setRequestProperty("Authorization", author);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.connect();
        //发送
        OutputStream out = new DataOutputStream(connection.getOutputStream());
        // 写入请求的字符串
        System.out.println(json.toString());
        out.write((json.toString()).getBytes("UTF-8"));
        out.flush();
        out.close();

        System.out.println(connection.getResponseCode());
    }


    @Test
    public void testAddErrorVariable() {
        String inputPath = "SWH.nc";
        String commandStr = "cmd /c python addErrorVariable.py " + inputPath;
        System.out.println(commandStr);
        // String s = CMDUtils.executeCMD(commandStr);
        // System.out.println(s);
    }

    /**
     * 遍历目录下的所有文件
     */
    @Test
    public void testDeleteFolder() {
        // FileUtils.delDir("D:/oceantest/");

        File ncFolder = new File("D:/OceanVisualization/data/");
        File[] ncFilePath = ncFolder.listFiles();
        String property = System.getProperties().getProperty("os.name");

        for (File file : ncFilePath) {
            if (file.isFile()) {
                if (file.getName().endsWith(".nc")) {
                    if (property.toLowerCase().startsWith("win")) {
                        // todo 需要解决脚本路径问题
                        String commandStr = "cmd /c python addErrorVariable.py " + file;
                        // CMDUtils.executeCMD(commandStr);
                        System.out.println(commandStr);
                    } else {
                        // 执行 linux cmd
                        String commandStr = "python addErrorVariable.py " + file;
                        CMDUtils.executeCMD(commandStr);
                    }
                }
            }
        }
    }

    /**
     * 遍历目录文件
     * @throws IOException
     */
    @Test
    public void testTraverserFile() throws IOException {
        // 方式一
        // File ncFolder = new File(serverTempFilePath);
        // File[] ncFilePath = ncFolder.listFiles();
        // String property = System.getProperties().getProperty("os.name");
        //
        // for (File file : ncFilePath) {
        //     if (file.isFile()) {
        //         if (file.getName().endsWith(".nc")) {
        //             if (property.toLowerCase().startsWith("win")) {
        //                 // todo 需要解决脚本路径问题
        //                 String commandStr = "cmd /c python addErrorVariable.py " + file;
        //                 // CMDUtils.executeCMD(commandStr);
        //                 System.out.println(commandStr);
        //             } else {
        //                 // 执行 linux cmd
        //                 String commandStr = "python addErrorVariable.py " + file;
        //                 CMDUtils.executeCMD(commandStr);
        //             }
        //         }
        //     }
        // }

        // 方式二
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<File> files = Files.list(Paths.get("D:/OceanVisualization/data/"))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".nc"))
                .map(Path::toFile)
                .collect(Collectors.toList());
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());


        String property = System.getProperties().getProperty("os.name");

        files.forEach(new Consumer<File>() {
            @Override
            public void accept(File file) {
                if (property.toLowerCase().startsWith("win")) {
                    // todo 需要解决脚本路径问题
                    String commandStr = "cmd /c python addErrorVariable.py " + file;
                    // CMDUtils.executeCMD(commandStr);
                    System.out.println(commandStr);
                } else {
                    // 执行 linux cmd
                    String commandStr = "python addErrorVariable.py " + file;
                    CMDUtils.executeCMD(commandStr);
                }
            }
        });
    }

    /**
     * Guava缓存测试
     * @throws ExecutionException
     */
    @Test
    public void testCache() throws ExecutionException {
        LoadingCache<String, String> cache = CacheBuilder.newBuilder()
                .recordStats()
                .maximumSize(1000)
                .expireAfterAccess(10, TimeUnit.DAYS)
                .build(new CacheLoader<String, String>() {
                    @Override
                    public String load(String tpSeq) throws Exception {
                        System.out.println("cache 执行了");
                        return "AAA";
                    }
                });

        String geoJsonString = cache.get("a");
        System.out.println(geoJsonString);

        String s = cache.get("a");
        System.out.println(s);
    }

    /**
     * 台风序列计数测试
     */
    @SneakyThrows
    @Test
    public void testCountTyphoon() {
        ConcurrentHashMap<String, List> map = new ConcurrentHashMap<>();
        File typhoonFolder = new File("D:\\OceanVisualization\\data\\typhoon_data");
        File[] typhoonFilePath = typhoonFolder.listFiles();
        for (File file : typhoonFilePath) {
            if (file.getName().contains("EP")) {
                map.put("EP", getTxtFilesCount(file));
            } else if (file.getName().contains("WP")) {
                map.put("WP", getTxtFilesCount(file));
            } else if (file.getName().contains("NA")) {
                map.put("NA", getTxtFilesCount(file));
            }
        }
        // System.out.println(map);
        //
        // File file = new File("D:/user2.json");
        // if (!file.exists())
        //     file.createNewFile();
        // ObjectMapper mapper = new ObjectMapper();
        // mapper.writeValue(file, map);


        ObjectMapper objectMapper = new ObjectMapper();
        ConcurrentHashMap concurrentHashMap = objectMapper.readValue(new File("D:/user2.json"), ConcurrentHashMap.class);
        System.out.println(concurrentHashMap);

    }

    public static List<String> getTxtFilesCount(File srcFile) {
        ArrayList<String> list = new ArrayList<>();
        int count = 0;
        // 判断传入的文件是不是为空
        if (srcFile == null) {
            throw new NullPointerException();
        }
        // 把所有目录、文件放入数组
        File[] files = srcFile.listFiles();
        // 遍历数组每一个元素
        for (File f : files) {
            // 判断元素是不是文件夹，是文件夹就重复调用此方法（递归）
            if (f.isDirectory()) {
                getTxtFilesCount(f);
            } else {
                // 判断文件是不是以.txt结尾的文件，并且count++（注意：文件要显示扩展名）
                if (f.getName().endsWith(".txt")) {
                    count++;
                    list.add(f.getName());
                }
            }
        }
        // 返回.txt文件个数
        return list;
    }

    /**
     * 路径测试
     */
    @Test
    public void testPathTransform(){
        String path = "D:/aaa" + "/bbb.json";
        System.out.println(FilenameUtils.separatorsToSystem(path));

        // String scriptPath = "D:\\Java\\JavaEE\\IdeaProjects\\ocean-visualization\\ocean-visualization-service\\src\\main\\java\\com\\csu\\oceanvisualization\\scripts\\addErrorVariable.py";
        String projectPath = "D:/Java/JavaEE/IdeaProjects/";
        String scriptPath = "ocean-visualization/ocean-visualization-service/src/main/java/com/csu/oceanvisualization/scripts/addErrorVariable.py";
        System.out.println(FilenameUtils.separatorsToSystem(projectPath + scriptPath));
        // System.out.println(FilenameUtils.separatorsToUnix(scriptPath));


    }

}
