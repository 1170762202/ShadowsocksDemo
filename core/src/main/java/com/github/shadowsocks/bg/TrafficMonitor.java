package com.github.shadowsocks.bg;

import android.net.LocalSocket;
import android.os.SystemClock;
import android.util.Pair;
import com.github.shadowsocks.aidl.TrafficStats;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author 陈志鹏
 * @date 2019/2/22
 */
public class TrafficMonitor implements AutoCloseable {

    private File statFile;
    private boolean dirty = false;

    private LocalSocketListener thread = new LocalSocketListener("TrafficMonitor") {
        @NotNull
        @Override
        protected File getSocketFile() {
            return statFile;
        }

        @Override
        protected void accept(@NotNull LocalSocket socket) {
            try {
                byte[] buffer = new byte[16];
                if (socket.getInputStream().read(buffer) != 16) {
                    throw new IOException("Unexpected traffic stat length");
                }
                ByteBuffer stat = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN);
                long tx = stat.getLong(0);
                long rx = stat.getLong(8);

                Long txTotal = TrafficMonitor.this.getCurrent().getTxTotal();
                if (txTotal != tx) {
                    TrafficMonitor.this.getCurrent().setTxTotal(tx);
                    dirty = true;
                }
                Long rxTotal = getCurrent().getRxTotal();
                if (rxTotal != rx) {
                    getCurrent().setRxTotal(rx);
                    dirty = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @NotNull
    private TrafficStats current;
    @NotNull
    private TrafficStats out;
    private long timestampLast = 0;

    @NotNull
    public TrafficStats getOut() {
        return out;
    }

    @NotNull
    public TrafficStats getCurrent() {
        return current;
    }

    public TrafficMonitor(File statFile) {
        super();
        this.statFile = statFile;
        current = new TrafficStats();
        out = new TrafficStats();
        thread.start();
    }

    public Pair<TrafficStats, Boolean> requestUpdate() {
        long now = SystemClock.elapsedRealtime();
        long delta = now - timestampLast;
        timestampLast = now;
        boolean updated = false;
        if (delta != 0L) {
            if (dirty) {
                out = current.copy();
                current.setTxRate((current.getTxTotal() - out.getTxTotal()) * 1000 / delta);
                current.setRxRate((current.getTxTotal() - out.getTxTotal()) * 1000 / delta);
                dirty = false;
                updated = true;
            } else {
                if (out.getTxRate() != 0L) {
                    out.setTxRate(0L);
                    updated = true;
                }
                if (out.getRxRate() != 0L) {
                    out.setRxRate(0L);
                    updated = true;
                }
            }
        }
        return new Pair(out, updated);
    }

    @Override
    public void close() throws Exception {
        thread.stopThread();
    }
}
