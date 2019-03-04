package com.mrd.news.vpn.bean;

/**
 * @author 陈志鹏
 * @date 2019/2/22
 */
public class PingBean {
    /**
     * 进行ping操作的ip
     */
    private String ip;

    /**
     * 进行ping操作的次数
     */
    private int pingCount;

    /**
     * ping操作超时时间
     */

    private int pingWTime;

    /**
     * 存储ping操作后得到的数据
     */
    private StringBuffer resultBuffer;

    /**
     * ping ip花费的时间
     */
    private String pingTime = "0 ms";

    /**
     * 进行ping操作后的结果
     */
    private boolean result;

    /**
     * 记录adapter的位置
     */
    private int pos;

    public PingBean(String ip, int pingCount, int pingWTime, StringBuffer resultBuffer,int pos) {
        this.ip = ip;
        this.pingWTime = pingWTime;
        this.pingCount = pingCount;
        this.resultBuffer = resultBuffer;
        this.pos = pos;
    }

    public String getPingTime() {
        return pingTime;
    }

    public void setPingTime(String pingTime) {
        this.pingTime = pingTime;
    }

    public StringBuffer getResultBuffer() {
        return resultBuffer;
    }

    public void setResultBuffer(StringBuffer resultBuffer) {
        this.resultBuffer = resultBuffer;
    }

    public int getPingCount() {
        return pingCount;
    }

    public void setPingCount(int pingCount) {
        this.pingCount = pingCount;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public int getPingWTime() {
        return pingWTime;
    }

    public void setPingWTime(int pingWTime) {
        this.pingWTime = pingWTime;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
}
