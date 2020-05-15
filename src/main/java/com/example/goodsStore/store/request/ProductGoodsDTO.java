package com.example.goodsStore.store.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductGoodsDTO {

    private String operationId;

    private Integer productId;

    private Integer productStock;

    private Integer actualSales;

    private Integer sales;
    /**
     * 累计总销量增加值
     */
    private Integer productSalesNum;
    /**
     * 是否添加操作,用于计算累计销量、销量
     */
    private boolean add;
    /**
     * 通用操作库存，包括普通订单操作库存，商家操作库存，活动划拨库存等
     */
    private boolean common;
}
