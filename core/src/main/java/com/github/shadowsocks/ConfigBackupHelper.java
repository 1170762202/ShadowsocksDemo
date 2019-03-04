package com.github.shadowsocks;

import android.app.backup.BackupAgentHelper;
import android.app.backup.FileBackupHelper;
import com.github.shadowsocks.utils.Key;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public final class ConfigBackupHelper extends BackupAgentHelper {
    @Override
    public void onCreate() {
        this.addHelper("com.github.shadowsocks.database.profile",new FileBackupHelper(this,
                "../databases/" + Key.DB_PROFILE, "../databases/" + Key.DB_PUBLIC));
    }
}