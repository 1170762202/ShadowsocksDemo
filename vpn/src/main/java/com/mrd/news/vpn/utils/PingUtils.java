package com.mrd.news.vpn.utils;

import com.mrd.news.vpn.bean.PingBean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author 陈志鹏
 * @date 2019/2/22
 */
public class PingUtils {
    /**
     * @param pingBean 检测网络实体类
     * @return 检测后的数据
     */
    public static PingBean ping(PingBean pingBean) {
        String line = null;
        Process process = null;
        BufferedReader successReader = null;
        String command = "ping -c " + pingBean.getPingCount() + " -w " + pingBean.getPingWTime() + " " + pingBean.getIp();
//        String command = "ping -c " + pingCount + " " + host;
        try {
            process = Runtime.getRuntime().exec(command);
            if (process == null) {
                append(pingBean.getResultBuffer(), "ping fail:process is null.");
                pingBean.setPingTime(null);
                pingBean.setResult(false);
                return pingBean;
            }
            successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = successReader.readLine()) != null) {
                append(pingBean.getResultBuffer(), line);
                String time;
                if ((time = getTime(line)) != null) {
                    pingBean.setPingTime(time);
                }
            }
            int status = process.waitFor();
            if (status == 0) {
                append(pingBean.getResultBuffer(), "exec cmd success:" + command);
                pingBean.setResult(true);
            } else {
                append(pingBean.getResultBuffer(), "exec cmd fail.");
                pingBean.setPingTime(null);
                pingBean.setResult(false);
            }
            append(pingBean.getResultBuffer(), "exec finished.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
            if (successReader != null) {
                try {
                    successReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return pingBean;
    }

    private static void append(StringBuffer stringBuffer, String text) {
        if (stringBuffer != null) {
            stringBuffer.append(text + "\n");
        }
    }

    private static String getTime(String line) {
        String[] lines = line.split("\n");
        String time = null;
        for (String l : lines) {
            if (!l.contains("time=")) {
                continue;
            }
            int index = l.indexOf("time=");
            time = l.substring(index + "time=".length());
        }
        return time;
    }
}
