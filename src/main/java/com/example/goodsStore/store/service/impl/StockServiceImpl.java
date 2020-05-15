package com.example.goodsStore.store.service.impl;

import com.example.goodsStore.store.repository.StoreRepository;
import com.example.goodsStore.store.request.StoreRequest;
import com.example.goodsStore.store.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Service
public class StockServiceImpl implements StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Override
    public boolean addStore(StoreRequest request) {
        storeRepository.addStore(request);
        return true;
    }

    @Override
    public boolean subStore(StoreRequest request) {
        storeRepository.subStore(request);
        return true;
    }

    @Override
    public boolean rollBackStore(StoreRequest request) {
        storeRepository.rollBackStore(request);
        return true;
    }
}
