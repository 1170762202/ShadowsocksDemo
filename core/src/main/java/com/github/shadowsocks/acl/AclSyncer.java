package com.github.shadowsocks.acl;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;
import com.github.shadowsocks.Core;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public class AclSyncer extends Worker {
    private static final String KEY_ROUTE = "route";

    public AclSyncer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static final Operation schedule(@NotNull String route) {
        Intrinsics.checkParameterIsNotNull(route, KEY_ROUTE);
        WorkManager var5 = WorkManager.getInstance();
        OneTimeWorkRequest.Builder var6 = new OneTimeWorkRequest.Builder(AclSyncer.class);
        var6.setInputData((new Data.Builder()).putString(KEY_ROUTE, route).build());
        var6.setConstraints((new Constraints.Builder()).setRequiredNetworkType(NetworkType.UNMETERED).setRequiresCharging(true).build());
        var6.setInitialDelay(10L, TimeUnit.SECONDS);
        OneTimeWorkRequest var7 = (OneTimeWorkRequest) var6.build();
        Operation var10000 = var5.enqueue((WorkRequest) var7);
        return var10000;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String route = getInputData().getString(KEY_ROUTE);
            if (route == null) {
                Intrinsics.throwNpe();
            }

            Intrinsics.checkExpressionValueIsNotNull(route, "inputData.getString(KEY_ROUTE)!!");
            InputStream inputStream = (new URL("https://shadowsocks.org/acl/android/v1/" + route + ".acl")).openStream();
            Intrinsics.checkExpressionValueIsNotNull(inputStream, "URL(\"https://shadowsocks…$route.acl\").openStream()");
            Reader reader = (Reader) (new InputStreamReader(inputStream, Charsets.UTF_8));
            Closeable var31 = (Closeable) (reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, 8192));
            BufferedReader it = (BufferedReader) var31;
            String text = TextStreamsKt.readText((Reader) it);

            File var32 = Acl.getFile(route, Core.getDeviceStorage());
            OutputStream var9 = (OutputStream) (new FileOutputStream(var32));
            Writer writer = (Writer) (new OutputStreamWriter(var9, Charsets.UTF_8));
            BufferedWriter var10 = writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer, 8192);
            PrintWriter var34 = new PrintWriter(var10);
            var34.write(text);

            reader.close();
            writer.close();

            return Result.success();
        } catch (IOException var28) {
            var28.printStackTrace();
            return Result.retry();
        } catch (Exception var29) {
            var29.printStackTrace();
            return Result.failure();
        }
    }
}
