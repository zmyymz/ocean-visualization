package com.csu.oceanvisualization.utils;

import com.csu.oceanvisualization.servicebase.exceptionhandler.OceanException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author zmy
 * @version V1.0
 * @Package com.csu.oceanvisualization.utils
 * @date 2022/2/19 15:38
 */
public class CMDUtils {
    /**
     * 执行简单cmd命令
     *
     * @param command
     */
    public static String executeCMD(String command) {
        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(command);
            br = new BufferedReader(new InputStreamReader(p.getInputStream(), "GB2312"));
            String line = null;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            // System.out.println(sb.toString());
            return sb.toString();
        } catch (Exception e) {
            // e.printStackTrace();
            throw new OceanException(20001, "cmd命令执行异常");
        }
    }
}
