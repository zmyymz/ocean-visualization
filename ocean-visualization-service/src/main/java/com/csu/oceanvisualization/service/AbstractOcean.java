package com.csu.oceanvisualization.service;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.service
 * @date 2022/2/22 13:50
 */
public abstract class AbstractOcean {
    private final static String USER_NCFILE_PATH = "";

    private final static String SERVER_NCFILE_PATH = "";

    public final void publishOceanLayer(){
        // 1.文件目录处理
        traverseFile();

        // 2.生成两个新的变量
        calculateError();

        // 3.将nc转为tif
        gdalTranslate();

        // 4. geoserver发布tif图层
        publishTifLayer();

        // 5. 清理tif文件目录
        deleteTifFile();
    }


    protected abstract void traverseFile();

    protected abstract void calculateError();

    protected abstract void gdalTranslate();

    protected abstract void publishTifLayer();

    protected abstract void deleteTifFile();



}
