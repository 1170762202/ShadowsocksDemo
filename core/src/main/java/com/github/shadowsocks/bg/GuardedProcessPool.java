package com.github.shadowsocks.bg;

import android.os.Build;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import androidx.annotation.Nullable;
import com.github.shadowsocks.Core;
import com.github.shadowsocks.utils.Commandline;
import com.github.shadowsocks.utils.UtilsKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import kotlin.io.FilesKt;
import kotlin.jvm.functions.Function0;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author chenzhipeng
 */
public final class GuardedProcessPool {
    private final AtomicReference<HashSet<Thread>> guardThreads = new AtomicReference(new HashSet());
    private static final String TAG = "GuardedProcessPool";

    private final class Guard {
        private List<String> cmd;
        private Function0 onRestartCallback;
        @NotNull
        private String cmdName;
        @NotNull
        private ArrayBlockingQueue<IOException> excQueue;
        private boolean pushed = false;

        public Guard(List<String> cmd, Function0 onRestartCallback) {
            super();
            this.cmd = cmd;
            this.onRestartCallback = onRestartCallback;
            this.cmdName = FilesKt.getNameWithoutExtension(new File((String) CollectionsKt.first(this.cmd)));
            this.excQueue = new ArrayBlockingQueue<IOException>(1);
        }

        private void pushException(IOException ioException) {
            if (pushed){
                return;
            }
            if (ioException == null){
                ioException = new Dummy();
            }
            try {
                excQueue.put(ioException);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pushed = true;
        }

        @NotNull
        public String getCmdName() {
            return cmdName;
        }

        @NotNull
        public ArrayBlockingQueue getExcQueue() {
            return excQueue;
        }

        public void looper(@NotNull HashSet<Thread> host){
            Process process = null;
            try {
                Function0 callback = null;
                while (guardThreads.get() == host) {
                    Log.e(TAG, "start process: " + Commandline.toString(cmd));
                    long startTime = SystemClock.elapsedRealtime();

                    process = new ProcessBuilder(cmd)
                            .redirectErrorStream(true)
                            .directory(Core.getDeviceStorage().getNoBackupFilesDir())
                            .start();

                    if (callback == null) {
                        callback = onRestartCallback;
                    } else {
                        callback.invoke();
                    }

                    pushException(null);
                    process.waitFor();

                    if (SystemClock.elapsedRealtime() - startTime < 1000) {
                        Log.e(TAG, "process exit too fast, stop guard:" + cmdName);
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "thread interrupt, destroy process:" + cmdName);
            } catch (IOException e) {
                pushException(e);
            } finally {
                if (process != null) {
                    if (Build.VERSION.SDK_INT < 24) {
                        try {
                            int pid = (Integer) GuardedProcessPool.Dummy.getPid().get(process);
                            Os.kill(pid, OsConstants.SIGTERM);
                            Object mutex = GuardedProcessPool.Dummy.getExitValueMutex().get(process);
                            synchronized (mutex) {
                                try {
                                    process.exitValue();
                                } catch (IllegalThreadStateException e) {
                                    try {
                                        mutex.wait(500);
                                    } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        } catch (ErrnoException e) {
                            if (e.errno != OsConstants.ESRCH){
                                e.printStackTrace();
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    process.destroy(); // kill the process

                    if (Build.VERSION.SDK_INT >= 26) {
                        boolean isKilled = false; // wait for 1 second
                        try {
                            isKilled = process.waitFor(1L, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!isKilled) {
                            process.destroyForcibly(); // Force to kill the process if it's still alive
                        }
                    }

                    try {
                        process.waitFor();   // ensure the process is destroyed
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                pushException(null);
            }
        }
    }

    public static class Dummy extends IOException {

        public Dummy() {
            super("Oopsie the developer has made a no-no");
        }

        public static Class getProcessImpl() {
            try {
                return Class.forName("java.lang.ProcessManager$ProcessImpl");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static Field getPid() {
            Field var1 = null;
            try {
                var1 = Dummy.getProcessImpl().getDeclaredField("pid");
                var1.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return var1;
        }

        public static Field getExitValueMutex() {
            Field var1 = null;
            try {
                var1 = Dummy.getProcessImpl().getDeclaredField("exitValueMutex");
                var1.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return var1;
        }
    }

    public final GuardedProcessPool start(@NotNull List<String> cmd, @Nullable Function0 onRestartCallback) {
        try {
            final Guard guard = new Guard(cmd, onRestartCallback);
            final HashSet<Thread> guardThreads = this.guardThreads.get();
            synchronized (guardThreads) {
                guardThreads.add(UtilsKt.thread("GuardThread-" + guard.cmdName, true, false, null, -1, new Function0() {
                    @Override
                    public Object invoke() {
                        guard.looper(guardThreads);
                        return Unit.INSTANCE;
                    }
                }));
            }
            IOException ioException = guard.excQueue.take();
            if (!(ioException instanceof Dummy)) {
                throw ioException;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public void killAll() {
        HashSet<Thread> guardThreads = this.guardThreads.getAndSet(new HashSet());
        synchronized(guardThreads) {
            Iterator iterator1 = guardThreads.iterator();
            while (iterator1.hasNext()){
                Thread thread = (Thread) iterator1.next();
                thread.interrupt();
            }
            try {
                Iterator iterator2 = guardThreads.iterator();
                while (iterator2.hasNext()){
                    Thread thread = (Thread) iterator2.next();
                    thread.join();
                }
            } catch (InterruptedException e) {
            }
        }
    }
}