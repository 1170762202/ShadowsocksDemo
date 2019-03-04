package com.github.shadowsocks.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

import com.github.shadowsocks.Core;
import com.github.shadowsocks.database.migration.RecreateSchemaMigration;
import com.github.shadowsocks.utils.Key;

import org.jetbrains.annotations.NotNull;

/**
 * @author 陈志鹏
 * @date 2019/2/25
 */
@Database(
        entities = {Profile.class, KeyValuePair.class},
        version = 27
)
public abstract class PrivateDatabase extends RoomDatabase {

    private static Profile.Dao profileDao;
    private static KeyValuePair.Dao kvPairDao;

    private static PrivateDatabase getInstance() {
        return Room.databaseBuilder(Core.app, PrivateDatabase.class, Key.DB_PROFILE)
                .addMigrations(
                        new Migration26(),
                        new Migration27())
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }

    public static Profile.Dao getProfileDao() {
        if (profileDao == null){
            profileDao = PrivateDatabase.getInstance().profileDao();
        }
        return profileDao;
    }

    public static com.github.shadowsocks.database.KeyValuePair.Dao getKvPairDao() {
        if (kvPairDao == null){
            kvPairDao = PrivateDatabase.getInstance().keyValuePairDao();
        }
        return kvPairDao;
    }

    @NotNull
    public abstract Profile.Dao profileDao();

    @NotNull
    public abstract KeyValuePair.Dao keyValuePairDao();

    public static final class Migration26 extends RecreateSchemaMigration {

        public Migration26() {
            super(25, 26, "Profile",
                    "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `host` TEXT NOT NULL, `remotePort` INTEGER NOT NULL, `password` TEXT NOT NULL, `method` TEXT NOT NULL, `route` TEXT NOT NULL, `remoteDns` TEXT NOT NULL, `proxyApps` INTEGER NOT NULL, `bypass` INTEGER NOT NULL, `udpdns` INTEGER NOT NULL, `ipv6` INTEGER NOT NULL, `individual` TEXT NOT NULL, `tx` INTEGER NOT NULL, `rx` INTEGER NOT NULL, `userOrder` INTEGER NOT NULL, `plugin` TEXT)",
                    "`id`, `name`, `host`, `remotePort`, `password`, `method`, `route`, `remoteDns`, `proxyApps`, `bypass`, `udpdns`, `ipv6`, `individual`, `tx`, `rx`, `userOrder`, `plugin`");
        }

        @Override
        public void migrate(SupportSQLiteDatabase database) {
            super.migrate(database);
            new PublicDatabase.Migration3().migrate(database);
        }
    }

    public static final class Migration27 extends Migration {

        /**
         * Creates a new migration between {@code startVersion} and {@code endVersion}.
         */
        public Migration27() {
            super(26, 27);
        }

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `Profile` ADD COLUMN `udpFallback` INTEGER");
        }
    }
}
