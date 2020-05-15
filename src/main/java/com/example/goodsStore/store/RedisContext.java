package com.example.goodsStore.store;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
public class RedisContext {

    public static String STOREFIX = "redis:store";

    public static String generatorCacheKey(String key) {
        return STOREFIX + ":" + key;
    }
}
