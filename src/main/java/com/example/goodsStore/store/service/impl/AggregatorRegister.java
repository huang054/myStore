package com.example.goodsStore.store.service.impl;


import com.example.goodsStore.store.listener.EventSource;
import com.example.goodsStore.store.mapper.ProductGoodsMapper;
import com.example.goodsStore.store.request.ProductGoodsDTO;
import com.example.goodsStore.store.service.ProductAggregatorStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Service
class AggregatorRegister {
    @Value("${dynamic.update.stock.mysql.update.limit:1000}")
    private Integer mysqlUpdateLimit;

    @Value("${dynamic.update.product.log.length.limit:1000}")
    private Integer productLogLengthLimit;


    @Autowired
    private ProductGoodsMapper productGoodsMapper;

    @Autowired
    private StockQueueBiz<ProductGoodsDTO> stockQueueBiz;

    @PostConstruct
    private void init() {

        EventSource<ProductGoodsDTO> s3 = new ProductAggregatorStrategy(mysqlUpdateLimit, productGoodsMapper);

        stockQueueBiz.addListener(s3);

    }
}

