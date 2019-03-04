package com.github.shadowsocks.database.migration;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;

import org.jetbrains.annotations.NotNull;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public class RecreateSchemaMigration extends Migration {
    private final String table;
    private final String schema;
    private final String keys;

    @Override
    public void migrate(@NotNull SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE `tmp` (" + this.schema + ")");
        database.execSQL("INSERT INTO `tmp` (" + this.keys + ") SELECT " + this.keys + " FROM `" + this.table + '`');
        database.execSQL("DROP TABLE `" + this.table + '`');
        database.execSQL("ALTER TABLE `tmp` RENAME TO `" + this.table + '`');
    }

    public RecreateSchemaMigration(int oldVersion, int newVersion, @NotNull String table, @NotNull String schema, @NotNull String keys) {
        super(oldVersion, newVersion);
        this.table = table;
        this.schema = schema;
        this.keys = keys;
    }
}