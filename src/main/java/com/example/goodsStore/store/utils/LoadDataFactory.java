package com.example.goodsStore.store.utils;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.CacheLoader;
import com.alicp.jetcache.CacheValueHolder;

import com.example.goodsStore.store.RedisConstant;
import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;


import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Slf4j
public
class LoadDataFactory {
    void loadWithExpiredTime(Cache<String, Object> cache, RedisClient redisClient,
                             String prefix, Function<String,Object> init, int second) {
        cache.config().setLoader(new CacheLoader<String, Object>() {
            @Override
            public Object load(String key) {
                return LettuceUtils.lockSyncLoad(redisClient, key, RedisConstant.LOCK_EXPIRE_TIME, (c) -> {
                    Object r = init.apply(prefix + key);
                    //定义一个公共的 空值
                    Object o = Optional.ofNullable(r).map(s->(Object) String.valueOf(r)).orElse(Empty.empty);
                    if (Empty.isEmptyType(o)) {
                        cache.put(key, o, RedisConstant.EMPTY_EXPIRE_TIME, TimeUnit.SECONDS);
                    } else {
                        cache.put(key, o, second, TimeUnit.SECONDS);
                    }
                    log.info("redis懒加载key:{},value:{}", key,o);
                    return o;
                }, (c) -> {
                    Object value = c.get((prefix+key).getBytes(StandardCharsets.UTF_8));
                    if (value == null) {
                        log.info("redis获取到空值:{}", key);
                        return null;
                    } else {
                        return ((CacheValueHolder) ValueDecoder.INSTANCE.apply((byte[]) value)).getValue();
                    }
                });
            }

            /**
             * 使用分布式锁 就不能在 通过jetCache 来更新了
             */
            @Override
            public boolean vetoCacheUpdate() {
                return true;
            }
        });
    }

    public void lazeInit(Cache<String, Object> cache, Set<String> keys) {
        Map<String, Object> values = cache.getAll(keys);
        for (Map.Entry<String, Object> entries : values.entrySet()) {
            String key = entries.getKey();
            Object value = entries.getValue();
            // 如果redis被强制设置了一个null值则会导致扣减库存失败
            if (GoodsStockUtil.illegalStock(value)) {
                throw new RuntimeException("货品key:" + key + "不存在");
            }
        }
    }
}

