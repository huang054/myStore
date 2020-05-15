package com.example.goodsStore.store.utils;

import com.example.goodsStore.store.RedisConstant;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.RedisCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.util.CollectionUtils;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Slf4j
public abstract class LettuceUtils {

    private static final RedisCodec JET_CACHE_CODEC = new JetCacheCodec();
    private static final String OK = "OK";

    public static String idempotentMaker(String operationId , boolean isAdd ){
        return RedisConstant.Key.STOCK_IDEMPOTENT_PRE + operationId
                + (isAdd ? RedisConstant.Key.STOCK_IDEMPOTENT_ADD_SUF : RedisConstant.Key.STOCK_IDEMPOTENT_SUB_SUF);
    }

    public static boolean tryLock(GenericObjectPool<StatefulRedisConnection<String, String>> redisPool, String key, long seconds) {
        try (StatefulRedisConnection<String, String> connect = redisPool.borrowObject()) {
            return OK.equals(connect.sync().set(key, Boolean.TRUE.toString(), SetArgs.Builder.ex(seconds).nx()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void expireIdempotent(GenericObjectPool<StatefulRedisConnection<String, String>> redisPool, String operationId, long seconds) {
        try (StatefulRedisConnection<String, String> connect = redisPool.borrowObject()) {
            // 更新防重key过期时间
            connect.sync().expire(operationId, seconds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void expireIdempotent(RedisCommands<String, String> sync, String operationId, long seconds) {
        // 更新防重key过期时间
        sync.expire(operationId, seconds);
    }

    /**
     * 加锁执行 单实例 或 负载
     *
     * @param redisClient
     * @param lockKeyPre
     * @param second
     * @param load
     * @param get
     * @param <T>
     * @return
     */
    public static <T> T lockSyncLoad(RedisClient redisClient,
                                     String lockKeyPre,
                                     Integer second,
                                     Function<RedisCommands<Object, Object>, T> load,
                                     Function<RedisCommands<Object, Object>, T> get) {
        int i = 50;
        //连接
        //同步执行命令
        RedisCommands<Object, Object> sync = null;
        //锁 key
        byte[] lockKey = (lockKeyPre + RedisConstant.Key.LOCK_SUF).getBytes(StandardCharsets.UTF_8);
        //锁结果
        String lockResult = null;
        //结果
        T result = null;
        StatefulRedisConnection<Object, Object> connect = null;
        // 不能使用自动关闭资源语法糖，因为后面还需要当前连接删除key
        try {
            connect = redisClient.connect(JET_CACHE_CODEC);
            sync = connect.sync();
            lockResult = sync.set(lockKey, "lock".getBytes(StandardCharsets.UTF_8), SetArgs.Builder.ex(second).nx());
            if (OK.equals(lockResult)) {
                result = load.apply(sync);
            } else {
                while (result == null) {
                    try {
                        //睡 50毫秒等待
                        TimeUnit.MILLISECONDS.sleep(50L);
                    } catch (InterruptedException e) {
                        log.error("错误", e);
                    }
                    result = get.apply(sync);

                    if (--i == 0 && result == null) {
                        result = (T) Empty.empty;
                    }
                }
            }
            return result;
        } finally {
            if (lockResult != null) {
                sync.del(new Object[]{lockKey});
            }
            if (connect != null) {
                connect.close();
            }
        }
    }

    /**
     * scan
     *
     * @param redisPool
     */
    public static List<KeyValue<String, String>> scanAli(
            GenericObjectPool<StatefulRedisConnection<String, String>> redisPool,
            String match, Integer limit) {
        try (StatefulRedisConnection<String, String> connect = redisPool.borrowObject()) {
            List<KeyValue<String, String>> result = new ArrayList<KeyValue<String, String>>();
            RedisCommands<String, String> sync = connect.sync();
            ScanArgs scanArgs = ScanArgs.Builder.limit(limit).match(match);
            ScanCursor cursor = ScanCursor.INITIAL;
            while (!cursor.isFinished()) {
                cursor = sync.scan(cursor, scanArgs);
                List<String> keys = ((KeyScanCursor) cursor).getKeys();
                if (!CollectionUtils.isEmpty(keys)) {
                    List<KeyValue<String, String>> mget = sync.mget(keys.toArray(new String[]{}));
                    result.addAll(mget);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("获取redis链接失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * scan
     *
     * @param redisPool
     */
    public static List<String> scanAliKey(
            GenericObjectPool<StatefulRedisConnection<String, String>> redisPool,
            String match, Integer limit) {
        try (StatefulRedisConnection<String, String> connect = redisPool.borrowObject()) {
            List<String> keys = new ArrayList<>();
            RedisCommands<String, String> sync = connect.sync();
            ScanArgs scanArgs = ScanArgs.Builder.limit(limit).match(match);
            ScanCursor cursor = ScanCursor.INITIAL;
            while (!cursor.isFinished()) {
                cursor = sync.scan(cursor, scanArgs);
                keys.addAll(((KeyScanCursor) cursor).getKeys());
            }
            return keys;
        } catch (Exception e) {
            log.error("获取redis链接失败", e);
            throw new RuntimeException(e);
        }
    }
}
