package com.github.shadowsocks.plugin;

import android.database.MatrixCursor;
import android.net.Uri;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @date: 2019\2\22 0022
 * @author: zlx
 * @description:
 */
public class PathProvider {
    private String basePath;
    private MatrixCursor cursor;

    public PathProvider(@NotNull Uri baseUri, @NotNull MatrixCursor cursor) {
        String path = baseUri.getPath();
        basePath = path != null ? StrUtil.trimFirstAndLastChar(path, '/') : "";
        this.cursor = cursor;
    }

    public PathProvider addPath(@NotNull String path, int mode) {
        String trimmed = StrUtil.trimFirstAndLastChar(path, '/');
        if (trimmed.startsWith(basePath)) {
            cursor.newRow().add(PluginContract.COLUMN_PATH, trimmed)
                    .add(PluginContract.COLUMN_MODE, mode);
        }
        return this;
    }

    public PathProvider addTo(@NotNull File file, @NotNull String to, int mode) {
        String sub = to + file.getName();
        if (basePath.startsWith(sub)) {
            if (file.isDirectory()) {
                sub += '/';
                for (File listFile : file.listFiles()) {
                    addTo(listFile, sub, mode);
                }
            } else {
                addPath(sub, mode);
            }
        }
        return this;
    }

    public PathProvider addAt(File file, String at, int mode) {
        if (basePath.startsWith(at)) {
            if (file.isDirectory()) {
                for (File listFile : file.listFiles()) {
                    addTo(listFile, at, mode);
                }
            } else {
                addPath(at, mode);
            }
        }
        return this;
    }
}
