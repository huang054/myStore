package com.example.goodsStore.store.listener;

import java.util.List;

/**
 * @author yangchen
 * @date 2019-10-17 10:15
 */
public interface EventSource<T> {
    /**
     * 消费事件
     * @param t
     */
    void consumer(List<T> t);
}
