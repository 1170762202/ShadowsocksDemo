package com.github.shadowsocks.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author 陈志鹏
 * @date 2019/2/27
 */
public abstract class ArrayIterator<T> implements Iterator<T> {

    public abstract int getSize();

    public abstract T get(int index);

    private int count = 0;

    @Override
    public boolean hasNext() {
        return this.count < this.getSize();
    }

    @Override
    public T next() {
        if (this.hasNext()) {
            int var1 = this.count++;
            return this.get(var1);
        } else {
            throw new NoSuchElementException();
        }
    }
}
