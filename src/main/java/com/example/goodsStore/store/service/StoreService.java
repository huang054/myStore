package com.example.goodsStore.store.service;

import com.example.goodsStore.store.request.StoreRequest;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
public interface StoreService {

    boolean addStore(StoreRequest request);

    boolean subStore(StoreRequest request);

    boolean rollBackStore(StoreRequest request);

}
