package com.github.shadowsocks.plugin;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * @date: 2019\2\22 0022
 * @author: zlx
 * @description:
 */
public abstract class NativePluginProvider extends ContentProvider {


    @Override
    public boolean onCreate() {
        return true;
    }

    protected abstract void populateFiles(@NotNull PathProvider var1);

    public String getExecutable() {
        throw new UnsupportedOperationException();
    }

    public abstract ParcelFileDescriptor openFile(@org.jetbrains.annotations.Nullable Uri var1);

    public ParcelFileDescriptor openFile(@org.jetbrains.annotations.Nullable Uri uri, @org.jetbrains.annotations.Nullable String mode) {
        if (!(mode == "r")) {
            String var4 = "Check failed.";
            throw new IllegalStateException(var4);
        } else {
            return this.openFile(uri);
        }
    }

    public Bundle call(@NotNull String method, @org.jetbrains.annotations.Nullable String arg,
                       @org.jetbrains.annotations.Nullable Bundle extras) {
        switch (method) {
            case PluginContract.METHOD_GET_EXECUTABLE: {
                Bundle bundle = new Bundle();
                bundle.putString(PluginContract.EXTRA_ENTRY,getExecutable());
                return bundle;
            }
            default: {
                return super.call(method, arg, extras);
            }
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (!(selection == null && selectionArgs == null && sortOrder == null)) {
            String var7 = "Check failed.";
            throw new IllegalStateException(var7);
        } else {
            MatrixCursor result = new MatrixCursor(projection);
            this.populateFiles(new PathProvider(uri, result));
            return result;
        }
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "application/x-elf";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
