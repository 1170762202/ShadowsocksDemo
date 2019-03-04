package com.github.shadowsocks.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kotlin.Deprecated;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;

/**
 * @author 陈志鹏
 * @date 2019/2/26
 */
@Entity
public class KeyValuePair {
    public static final int TYPE_UNINITIALIZED = 0;
    public static final int TYPE_BOOLEAN = 1;
    public static final int TYPE_FLOAT = 2;
    /**
     * @deprecated
     */
    public static final int TYPE_INT = 3;
    public static final int TYPE_LONG = 4;
    public static final int TYPE_STRING = 5;
    public static final int TYPE_STRING_SET = 6;

    @android.arch.persistence.room.Dao
    public interface Dao {
        @Query("SELECT * FROM `KeyValuePair` WHERE `key` = :key")
        @Nullable
        KeyValuePair get(@NotNull String key);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        long put(@NotNull KeyValuePair value);

        @Query("DELETE FROM `KeyValuePair` WHERE `key` = :key")
        int delete(@NotNull String key);
    }

    @PrimaryKey
    @NotNull
    private String key = "";
    private int valueType = TYPE_UNINITIALIZED;
    @NotNull
    private byte[] value = new byte[0];

    @Nullable
    public final Boolean getBoolean() {
        return this.valueType == 1 ? ByteBuffer.wrap(this.value).get() != (byte) 0 : null;
    }

    @Nullable
    public final Float getFloat() {
        Float var1;
        if (this.valueType == 2) {
            var1 = ByteBuffer.wrap(this.value).getFloat();
        } else {
            var1 = null;
        }
        return var1;
    }

    /**
     * @deprecated
     */
    @Nullable
    public final Integer getInt() {
        Integer var1;
        if (this.valueType == 3) {
            var1 = ByteBuffer.wrap(this.value).getInt();
        } else {
            var1 = null;
        }

        return var1;
    }

    @Nullable
    public final Long getLong() {
        int var1 = this.valueType;
        Long var2;
        if (var1 == 3) {
            var2 = (long) ByteBuffer.wrap(this.value).getInt();
        } else if (var1 == 4) {
            var2 = ByteBuffer.wrap(this.value).getLong();
        } else {
            var2 = null;
        }

        return var2;
    }

    @Nullable
    public final String getString() {
        String var10000;
        if (this.valueType == 5) {
            byte[] var1 = this.value;
            var10000 = new String(var1, Charsets.UTF_8);
        } else {
            var10000 = null;
        }

        return var10000;
    }

    @Nullable
    public final Set getStringSet() {
        Set var10000;
        if (this.valueType == 6) {
            ByteBuffer buffer = ByteBuffer.wrap(this.value);
            HashSet result = new HashSet();

            while (buffer.hasRemaining()) {
                byte[] chArr = new byte[buffer.getInt()];
                buffer.get(chArr);
                String var5 = new String(chArr, Charsets.UTF_8);
                result.add(var5);
            }

            var10000 = (Set) result;
        } else {
            var10000 = null;
        }

        return var10000;
    }

    public KeyValuePair() {

    }

    @Ignore
    public KeyValuePair(@NotNull String key) {
        this();
        this.key = key;
    }

    @NotNull
    public final KeyValuePair put(boolean value) {
        this.valueType = 1;
        byte[] var10001 = ByteBuffer.allocate(1).put((byte) (value ? 1 : 0)).array();
        this.value = var10001;
        return this;
    }

    @NotNull
    public final KeyValuePair put(float value) {
        this.valueType = 2;
        byte[] var10001 = ByteBuffer.allocate(4).putFloat(value).array();
        this.value = var10001;
        return this;
    }

    /**
     * @deprecated
     */
    @Deprecated(
            message = "Use long."
    )
    @NotNull
    public final KeyValuePair put(int value) {
        this.valueType = 3;
        byte[] var10001 = ByteBuffer.allocate(4).putInt(value).array();
        this.value = var10001;
        return this;
    }

    @NotNull
    public final KeyValuePair put(long value) {
        this.valueType = 4;
        byte[] var10001 = ByteBuffer.allocate(8).putLong(value).array();
        this.value = var10001;
        return this;
    }

    @NotNull
    public final KeyValuePair put(@NotNull String value) {
        Intrinsics.checkParameterIsNotNull(value, "value");
        this.valueType = 5;
        Charset var3 = Charsets.UTF_8;
        this.value = value.getBytes(var3);
        return this;
    }

    @NotNull
    public final KeyValuePair put(@NotNull Set value) {
        Intrinsics.checkParameterIsNotNull(value, "value");
        this.valueType = 6;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Iterator var4 = value.iterator();

        while (var4.hasNext()) {
            String v = (String) var4.next();
            try {
                stream.write(ByteBuffer.allocate(4).putInt(v.length()).array());
                Charset var6 = Charsets.UTF_8;
                if (v == null) {
                    throw new TypeCastException("null cannot be cast to non-null type java.lang.String");
                }

                byte[] var10000 = v.getBytes(var6);
                Intrinsics.checkExpressionValueIsNotNull(var10000, "(this as java.lang.String).getBytes(charset)");
                byte[] var8 = var10000;
                stream.write(var8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] var10001 = stream.toByteArray();
        this.value = var10001;
        return this;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    public void setKey(@NotNull String key) {
        this.key = key;
    }

    public int getValueType() {
        return valueType;
    }

    public void setValueType(int valueType) {
        this.valueType = valueType;
    }

    @NotNull
    public byte[] getValue() {
        return value;
    }

    public void setValue(@NotNull byte[] value) {
        this.value = value;
    }
}
