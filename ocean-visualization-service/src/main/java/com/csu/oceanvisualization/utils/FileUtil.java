package com.csu.oceanvisualization.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.utils
 * @date 2022/2/18 16:48
 */
public class FileUtil {
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
                    String name = f.getName();
                    String fileName = name.substring(0, name.lastIndexOf("."));
                    list.add(fileName);
                    // list.add(f.getName());
                }
            }
        }
        // 返回.txt文件个数
        return list;
    }


    /**
     * 计算文件的md5
     *
     * @param f 源文件
     * @return
     */
    public static String md5(File f) {
        try (FileInputStream fis = new FileInputStream(f)) {
            //消息摘要
            MessageDigest md = MessageDigest.getInstance("md5");

            byte[] bytes = new byte[2048];
            int len = 0;
            while ((len = fis.read(bytes)) != -1) {
                md.update(bytes, 0, len);
            }
            byte[] digest = md.digest();

            //16进制转换
            BigInteger bi = new BigInteger(1, digest);
            return bi.toString(16);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
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
            }else{
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
            FileUtil.copyFile(srcPath, newDestPath);
        }
    }

    /**
     * 拷贝文件
     *
     * @param srcPath     源文件
     * @param newDestPath 目的目录
     * @throws Exception
     */
    public static void copyFile(File srcPath, File newDestPath) throws Exception {
        try (
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(srcPath));
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(newDestPath));
        ) {
            byte[] data = new byte[1024];
            int length = 0;
            while ((length = in.read(data)) != -1) {
                out.write(data, 0, length);
            }
        }
    }

    /**
     * 获得某一个文件夹下所有文件名
     *
     * @param path
     * @return
     */
    public static String[] getFileName(String path) {
        File file = new File(path);
        String[] fileName = file.list();
        return fileName;
    }
}
