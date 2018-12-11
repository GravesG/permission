package com.mmall.util;

import org.apache.commons.lang3.StringUtils;

public class LevelUtil {
    //层级间隔符
    public final static String SEPARATOR = ".";

    public final static String ROOT = "0";

    public static String calculateLevel(String parentLevel, int parentId){
        if(StringUtils.isNotBlank(parentLevel)){
            return ROOT;
        } else {
            return StringUtils.join(parentLevel,SEPARATOR,parentId);
        }
    }
}
