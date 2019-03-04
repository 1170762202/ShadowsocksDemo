package com.github.shadowsocks.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import com.github.shadowsocks.Core;
import com.github.shadowsocks.database.migration.RecreateSchemaMigration;
import com.github.shadowsocks.utils.Key;

import org.jetbrains.annotations.NotNull;


/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
@Database(
        entities = {KeyValuePair.class},
        version = 3
)
public abstract class PublicDatabase extends RoomDatabase {

    private static PublicDatabase getInstance() {
        return Room.databaseBuilder(Core.getDeviceStorage(), PublicDatabase.class, Key.DB_PUBLIC)
                .allowMainThreadQueries()
                .addMigrations(
                        new Migration3()
                )
                .fallbackToDestructiveMigration()
                .build();
    }

    @NotNull
    public abstract KeyValuePair.Dao keyValuePairDao();

    @NotNull
    public static final KeyValuePair.Dao getKvPairDao() {
        return PublicDatabase.getInstance().keyValuePairDao();
    }

    public static final class Migration3 extends RecreateSchemaMigration {

        public Migration3() {
            super(2, 3, "KeyValuePair", "(`key` TEXT NOT NULL, `valueType` INTEGER NOT NULL, `value` BLOB NOT NULL, PRIMARY KEY(`key`))", "`key`, `valueType`, `value`");
        }
    }
}