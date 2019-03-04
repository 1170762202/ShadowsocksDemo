package com.github.shadowsocks.bg;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import com.github.shadowsocks.utils.UtilsKt;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public abstract class LocalSocketListener extends Thread {
    private volatile boolean running = true;
    @NotNull
    private final String tag;

    @NotNull
    protected abstract File getSocketFile();

    protected abstract void accept(@NotNull LocalSocket var1);

    public LocalSocketListener(String tag) {
        super(tag);
        this.tag = tag;
        setUncaughtExceptionHandler(null);
    }

    @Override
    public void run() {
        this.getSocketFile().delete();
        LocalSocket localSocket = new LocalSocket();

        try {
            LocalServerSocket serverSocket;
            try {
                localSocket.bind(new LocalSocketAddress(this.getSocketFile().getAbsolutePath(), LocalSocketAddress.Namespace.FILESYSTEM));
                serverSocket = new LocalServerSocket(localSocket.getFileDescriptor());
            } catch (IOException var26) {
                UtilsKt.printLog((Throwable) var26);
                return;
            }
            while (this.running) {
                LocalSocket var30;
                try {
                    var30 = serverSocket.accept();
                } catch (IOException var25) {
                    UtilsKt.printLog((Throwable) var25);
                    var30 = null;
                }

                if (var30 != null) {
                    LocalSocketListener var6 = this;
                    try {
                        ((LocalSocketListener) var6).accept(var30);
                    } catch (Throwable var23) {
                        throw var23;
                    } finally {

                    }
                }
            }
        } catch (Throwable var27) {
            throw var27;
        }
    }

    public final void stopThread() {
        this.running = false;
        this.interrupt();
    }

    @NotNull
    protected final String getTag() {
        return this.tag;
    }

}
