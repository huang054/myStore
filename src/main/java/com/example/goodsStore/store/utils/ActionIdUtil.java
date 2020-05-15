package com.example.goodsStore.store.utils;

import java.util.UUID;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
public class ActionIdUtil {

    public static String setActionId(){
        return setActionId(String.valueOf(System.currentTimeMillis()));
    }

    public static String setActionId(String key){
        return UUID.randomUUID()+key;
    }
}
