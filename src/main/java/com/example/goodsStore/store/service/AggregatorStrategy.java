package com.example.goodsStore.store.service;

import java.util.List;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
interface AggregatorStrategy<T, R> {
    /**
     * 数据聚合
     *
     * @param t
     * @return
     */
    List<R> aggregate(List<T> t);
}
