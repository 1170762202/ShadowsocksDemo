package com.github.shadowsocks.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public class TrafficStats implements Parcelable {
    // Bytes per second
    private Long txRate = 0L;
    private Long rxRate = 0L;

    // Bytes for the current session
    private Long txTotal = 0L;
    private Long rxTotal = 0L;

    public TrafficStats(long txRate, long rxRate, long txTotal, long rxTotal) {
        this.txRate = txRate;
        this.rxRate = rxRate;
        this.txTotal = txTotal;
        this.rxTotal = rxTotal;
    }

    public TrafficStats plus(TrafficStats other) {
        return new TrafficStats(
                txRate + other.txRate, rxRate + other.rxRate,
                txTotal + other.txTotal, rxTotal + other.rxTotal);
    }

    public TrafficStats copy() {
        return new TrafficStats(txRate, txRate, txTotal, rxTotal);
    }

    public Long getTxRate() {
        return txRate;
    }

    public void setTxRate(Long txRate) {
        this.txRate = txRate;
    }

    public Long getRxRate() {
        return rxRate;
    }

    public void setRxRate(Long rxRate) {
        this.rxRate = rxRate;
    }

    public Long getTxTotal() {
        return txTotal;
    }

    public void setTxTotal(Long txTotal) {
        this.txTotal = txTotal;
    }

    public Long getRxTotal() {
        return rxTotal;
    }

    public void setRxTotal(Long rxTotal) {
        this.rxTotal = rxTotal;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.txRate);
        dest.writeValue(this.rxRate);
        dest.writeValue(this.txTotal);
        dest.writeValue(this.rxTotal);
    }

    public TrafficStats() {
    }

    protected TrafficStats(Parcel in) {
        this.txRate = (Long) in.readValue(Long.class.getClassLoader());
        this.rxRate = (Long) in.readValue(Long.class.getClassLoader());
        this.txTotal = (Long) in.readValue(Long.class.getClassLoader());
        this.rxTotal = (Long) in.readValue(Long.class.getClassLoader());
    }

    public static final Creator<TrafficStats> CREATOR = new Creator<TrafficStats>() {
        @Override
        public TrafficStats createFromParcel(Parcel source) {
            return new TrafficStats(source);
        }

        @Override
        public TrafficStats[] newArray(int size) {
            return new TrafficStats[size];
        }
    };
}
