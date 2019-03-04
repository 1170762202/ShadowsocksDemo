package com.github.shadowsocks.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import kotlin.Suppress;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.Objects;

/**
 * @author 陈志鹏
 * @date 2019/2/26
 */
public class Subnet implements Comparable<Subnet> {

    private InetAddress address;
    private int prefixSize;

    private Subnet(InetAddress address, int prefixSize) {
        this.address = address;
        this.prefixSize = prefixSize;

        if (prefixSize < 0 || prefixSize > getAddressLength()) {
            throw new IllegalArgumentException("prefixSize: $prefixSize");
        }
    }

    public static Subnet fromString(@NotNull String value) {
        @Suppress(names = "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        String[] parts = value.split("/", 2);
        InetAddress addr = UtilsKt.parseNumericAddress(parts[0]);
        if (addr == null) {
            addr = null;
        }
        if (parts.length == 2) {
            try {
                int prefixSize = Integer.valueOf(parts[1]);
                if (prefixSize < 0 || prefixSize > addr.getAddress().length << 3) {
                    return null;
                } else {
                    return new Subnet(addr, prefixSize);
                }
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return new Subnet(addr, addr.getAddress().length << 3);
        }
    }

    private int getAddressLength() {
        return address.getAddress().length << 3;
    }

    @NonNull
    @Override
    public String toString() {
        if (prefixSize == getAddressLength()) {
            return address.getHostAddress();
        } else {
            return address.getHostAddress() + '/' + prefixSize;
        }
    }

    private int unsigned(byte data) {
        return data & 255;
    }

    @Override
    public int compareTo(Subnet other) {
        byte[] addrThis = address.getAddress();
        byte[] addrThat = other.address.getAddress();
        int result = 0;
        if (addrThis.length == addrThat.length) {
            result = 0;
        } else if (addrThis.length < addrThat.length) {
            result = -1;
        } else {
            result = 1;
        }
        if (result != 0) {
            return result;
        }
        int num = addrThis.length > addrThat.length ? addrThat.length : addrThis.length;
        for (int i = 0; i < num; i++) {
            byte x = addrThis[i];
            byte y = addrThat[i];
            if (unsigned(x) == unsigned(y)) {
                result = 0;
            } else if (unsigned(x) < unsigned(y)) {
                result = -1;
            } else {
                result = 1;
            }
            if (result != 0) {
                return result;
            }
        }
        if (prefixSize == other.prefixSize) {
            return 0;
        } else if (prefixSize < other.prefixSize) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(@Nullable Object other) {
        Subnet that = null;
        if (other instanceof Subnet){
            that = (Subnet) other;
        }
        if (that == null){
            return false;
        }else{
            return address == that.address && prefixSize == that.prefixSize;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, prefixSize);
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPrefixSize() {
        return prefixSize;
    }
}
