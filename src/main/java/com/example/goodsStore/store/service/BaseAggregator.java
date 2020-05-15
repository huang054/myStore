package com.example.goodsStore.store.service;

import com.example.goodsStore.store.listener.EventSource;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Slf4j
abstract class BaseAggregator<T, R> implements EventSource<T>, AggregatorStrategy<T, R> {
    private final int updateLimit;
    private final Consumer<R> consumer;

    BaseAggregator(int updateLimit, Consumer<R> consumer) {
        this.updateLimit = updateLimit;
        this.consumer = consumer;
    }

    @Override
    public void consumer(List<T> t) {
        if (CollectionUtils.isEmpty(t)) {
            return;
        }
        List<R> r = aggregate(t);
        if (CollectionUtils.isEmpty(r)) {
            log.debug("异步队列未获取到任何可更新的数据");
            return;
        }
       // log.info("异步队列更新数据，原始数量：{}，聚合后数量：{}, data:{}",
        //        r.size(), r.size(), logValue(r));
        List<List<R>> list = Lists.partition(r, updateLimit);
        list.forEach(c -> {
            c.forEach(consumer);
            if (c.size() == updateLimit) {
                try {
                    TimeUnit.MILLISECONDS.sleep(updateLimit);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        });
    }

}
