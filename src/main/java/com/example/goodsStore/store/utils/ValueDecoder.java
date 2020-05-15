package com.example.goodsStore.store.utils;

import com.alicp.jetcache.CacheValueHolder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Slf4j
public class ValueDecoder implements Function<byte[], Object> {

    public static final ValueDecoder INSTANCE = new ValueDecoder();

    @Override
    public Object apply(byte[] bytes) {
        CacheValueHolder<Object> v = new CacheValueHolder<>();
        // 由于jetCache 必须要设置过期时间 但是这些数据 又不是过期数据，所以只能将过期时间设置很大 下面为68年后过期.
        v.setExpireTime(2147483647000L);
        String value = new String(bytes, StandardCharsets.UTF_8);
        // 为了解决雪崩效应 虽然使用JetCache时 保存的是Empty 但是 实际存储的是null.
        String nullStr = "null";
        if(nullStr.equals(value)){
            v.setValue(Empty.empty);
        }else{
            v.setValue(value);
        }
        return v;
    }
}