package com.csu.oceanvisualization.utils;

import java.io.File;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.utils
 * @date 2022/2/18 16:48
 */
public class FileUtils {
    /**
     * 删除文件目录
     *
     * @param path
     */
    public static void delDir(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            File[] tmp = dir.listFiles();
            for (int i = 0; i < tmp.length; i++) {
                if (tmp[i].isDirectory()) {
                    delDir(path + File.separator + tmp[i].getName());
                } else {
                    tmp[i].delete();
                }
            }
            dir.delete();
        }
    }
}
