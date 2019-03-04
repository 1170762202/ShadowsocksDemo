package com.github.shadowsocks.database;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 陈志鹏
 * @date 2019/2/26
 */
public class JavaTest {
    @Test
    public void test(){
        ArrayList<Integer> list = new ArrayList<>();
        List<Integer> integers = Arrays.asList(0, 2, 6, 12, 20, 30);
        for (int i = 0; i < integers.size(); i++) {
            list.add(i * integers.get(i));
        }
        System.out.println(integers.toString());
        System.out.println(list.toString());
    }
}
