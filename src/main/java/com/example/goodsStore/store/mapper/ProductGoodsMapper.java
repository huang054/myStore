package com.example.goodsStore.store.mapper;


import com.example.goodsStore.store.request.ProductGoods;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Repository
public interface ProductGoodsMapper  {

    /**
     * 扣减活动货品库存
     * @param productGoods
     * @return
     */
    Integer updateActivityGoodsStock(@Param("productGoods") ProductGoods productGoods);
}
