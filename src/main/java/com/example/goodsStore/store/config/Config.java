package com.example.goodsStore.store.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Configuration
public class Config {

    @Bean(destroyMethod = "shutdown")
    public RedisClient stockRedisClient() {
        return redisClient("182.168.1.45", 6643,
                "password", Duration.ofDays(6));
    }

    @Bean
    public GenericObjectPool<StatefulRedisConnection<String, String>> redisPool() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(8);
        config.setMaxTotal(8);
        config.setMinIdle(0);
        config.setMaxWaitMillis(100000);
        config.setMinEvictableIdleTimeMillis(100000);
        config.setTimeBetweenEvictionRunsMillis(1000000);
        config.setTestOnBorrow(true);
        return ConnectionPoolSupport.createGenericObjectPool(stockRedisClient()::connect, config);
    }


    private RedisClient redisClient(String host, int port, String password, Duration timeout) {
        RedisURI redisUri = RedisURI.builder()
                .withDatabase(0)
                .withHost(host)
                .withPort(port)
                .withPassword(password)
                .withTimeout(timeout)
                .build();
        RedisClient redisClient = RedisClient.create(redisUri);
        redisClient.setOptions(ClientOptions.builder()
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build()
        );
        return redisClient;
    }
}
