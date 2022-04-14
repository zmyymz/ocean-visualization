package com.csu.oceanvisualization.service;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.service
 * @date 2022/3/5 11:06
 */
public abstract class AbstractTyphoon {

    public final void publishTyphoonLayer(){
        // 1.文件目录处理
        traverseFile();

        // 复制样式文件
        copyStyleFiles();

        // 2.统计每个WP文件夹下的txt数量
        countTyphoonSeq();

        // 3. geoserver发布tif图层
        publishTifLayer();

    }
    protected abstract void traverseFile();

    protected abstract void copyStyleFiles();

    protected abstract void countTyphoonSeq();

    protected abstract void publishTifLayer();

}
