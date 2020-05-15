package com.example.goodsStore.store.service;


import com.example.goodsStore.store.mapper.ProductGoodsMapper;
import com.example.goodsStore.store.request.ProductGoods;
import com.example.goodsStore.store.request.ProductGoodsDTO;

import java.util.ArrayList;
import java.util.List;


/**
 * @author HuangJ
 * @Date 2020/5/15
 */
public class ProductAggregatorStrategy extends BaseAggregator<ProductGoodsDTO, ProductGoods>{
    public ProductAggregatorStrategy(int updateLimit, ProductGoodsMapper productGoodsMapper) {
        super(updateLimit, productGoodsMapper::updateActivityGoodsStock);
    }

    @Override
    public List<ProductGoods> aggregate(List<ProductGoodsDTO> t) {

        return new ArrayList<ProductGoods>();
    }
}
