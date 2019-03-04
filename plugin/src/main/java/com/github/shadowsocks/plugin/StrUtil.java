package com.github.shadowsocks.plugin;

/**
 * @date: 2019\2\22 0022
 * @author: zlx
 * @description:
 */
public class StrUtil {
    //去掉首尾指定字符串
    public static String trimFirstAndLastChar(String source,char element){

        boolean beginIndexFlag = true;

        boolean endIndexFlag = true;

        do{

            int beginIndex = source.indexOf(element) == 0 ? 1 : 0;

            int endIndex = source.lastIndexOf(element) + 1 == source.length() ? source.lastIndexOf(element) : source.length();

            source = source.substring(beginIndex, endIndex);

            beginIndexFlag = (source.indexOf(element) == 0);

            endIndexFlag = (source.lastIndexOf(element) + 1 == source.length());

        } while (beginIndexFlag || endIndexFlag);

        return source;

    }
}
