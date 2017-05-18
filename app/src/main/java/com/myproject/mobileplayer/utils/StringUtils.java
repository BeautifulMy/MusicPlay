package com.myproject.mobileplayer.utils;

/**
 * Created by Administrator on 2017/3/8.
 */

public class StringUtils {
    public static String formatMedia(String name){
        int i = name.indexOf(".");
        String substring = name.substring(0,i-1);
        return substring;

    }
}
