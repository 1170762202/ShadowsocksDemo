package com.github.shadowsocks.preference;

import android.support.v7.preference.PreferenceDataStore;

import com.github.shadowsocks.database.KeyValuePair;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author 陈志鹏
 * @date 2019/2/21
 */
public class RoomPreferenceDataStore extends PreferenceDataStore {

    private final HashSet<OnPreferenceDataStoreChangeListener> listeners;
    private final KeyValuePair.Dao kvPairDao;

    public RoomPreferenceDataStore(@NotNull KeyValuePair.Dao kvPairDao) {
        super();
        this.kvPairDao = kvPairDao;
        this.listeners = new HashSet<OnPreferenceDataStoreChangeListener>();
    }

    @Nullable
    public final Boolean getBoolean(@NotNull String key) {
        KeyValuePair var10000 = this.kvPairDao.get(key);
        return var10000 != null ? var10000.getBoolean() : null;
    }

    @Nullable
    public final Float getFloat(@NotNull String key) {
        KeyValuePair var10000 = this.kvPairDao.get(key);
        return var10000 != null ? var10000.getFloat() : null;
    }

    @Nullable
    public final Integer getInt(@NotNull String key) {
        KeyValuePair var10000 = this.kvPairDao.get(key);
        Integer var3;
        if (var10000 != null) {
            Long var2 = var10000.getLong();
            if (var2 != null) {
                var3 = (int)var2.longValue();
                return var3;
            }
        }

        var3 = null;
        return var3;
    }

    @Nullable
    public final Long getLong(@NotNull String key) {
        KeyValuePair var10000 = this.kvPairDao.get(key);
        return var10000 != null ? var10000.getLong() : null;
    }

    @Nullable
    public final String getString(@NotNull String key) {
        KeyValuePair var10000 = this.kvPairDao.get(key);
        return var10000 != null ? var10000.getString() : null;
    }

    @Nullable
    public final Set getStringSet(@NotNull String key) {
        KeyValuePair var10000 = this.kvPairDao.get(key);
        return var10000 != null ? var10000.getStringSet() : null;
    }

    @Override
    public boolean getBoolean(@NotNull String key, boolean defValue) {
        Boolean var10000 = this.getBoolean(key);
        return var10000 != null ? var10000 : defValue;
    }

    @Override
    public float getFloat(@NotNull String key, float defValue) {
        Float var10000 = this.getFloat(key);
        return var10000 != null ? var10000 : defValue;
    }

    @Override
    public int getInt(@NotNull String key, int defValue) {
        Integer var10000 = this.getInt(key);
        return var10000 != null ? var10000 : defValue;
    }

    @Override
    public long getLong(@NotNull String key, long defValue) {
        Long var10000 = this.getLong(key);
        return var10000 != null ? var10000 : defValue;
    }

    @Override
    public String getString(@NotNull String key, @Nullable String defValue) {
        String var10000 = this.getString(key);
        if (var10000 == null) {
            var10000 = defValue;
        }

        return var10000;
    }

    @Override
    public Set getStringSet(@NotNull String key, @Nullable Set defValue) {
        Set var10000 = this.getStringSet(key);
        if (var10000 == null) {
            var10000 = defValue;
        }

        return var10000;
    }

    public final void putBoolean(@NotNull String key, @Nullable Boolean value) {
        if (value == null) {
            this.remove(key);
        } else {
            this.kvPairDao.put((new KeyValuePair(key)).put(value));
            this.fireChangeListener(key);
        }
    }

    public final void putFloat(@NotNull String key, @Nullable Float value) {
        if (value == null) {
            this.remove(key);
        } else {
            this.kvPairDao.put((new KeyValuePair(key)).put(value));
            this.fireChangeListener(key);
        }
    }

    public final void putInt(@NotNull String key, @Nullable Integer value) {
        if (value == null) {
            this.remove(key);
        } else {
            this.kvPairDao.put((new KeyValuePair(key)).put(value));
            this.fireChangeListener(key);
        }
    }

    public final void putLong(@NotNull String key, @Nullable Long value) {
        if (value == null) {
            this.remove(key);
        } else {
            this.kvPairDao.put((new KeyValuePair(key)).put(value));
            this.fireChangeListener(key);
        }
    }

    @Override
    public void putLong(String key, long value) {
        putLong(key, Long.valueOf(value));
    }

    @Override
    public void putString(@NotNull String key, @Nullable String value) {
        if (value == null) {
            this.remove(key);
        } else {
            this.kvPairDao.put((new KeyValuePair(key)).put(value));
            this.fireChangeListener(key);
        }

    }

    @Override
    public void putStringSet(@NotNull String key, @Nullable Set values) {
        if (values == null) {
            this.remove(key);
        } else {
            this.kvPairDao.put((new KeyValuePair(key)).put(values));
            this.fireChangeListener(key);
        }

    }

    public final void remove(@NotNull String key) {
        this.kvPairDao.delete(key);
        this.fireChangeListener(key);
    }

    private final void fireChangeListener(String key) {
        Iterator iterator = listeners.iterator();
        while(iterator.hasNext()) {
            OnPreferenceDataStoreChangeListener listener = (OnPreferenceDataStoreChangeListener) iterator.next();
            listener.onPreferenceDataStoreChanged((PreferenceDataStore)this, key);
        }
    }

    public final boolean registerChangeListener(@NotNull OnPreferenceDataStoreChangeListener listener) {
        return this.listeners.add(listener);
    }

    public final boolean unregisterChangeListener(@NotNull OnPreferenceDataStoreChangeListener listener) {
        return this.listeners.remove(listener);
    }
}
