package com.example.goodsStore.store.request;

import com.example.goodsStore.store.ActTypeEnum;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class ProductGoods implements Serializable {

    private static final long serialVersionUID = 438557000635478509L;


    private Integer id;

    /**
     * 活动ID
     */
    private String activityId;

    /**
     * 商品ID
     */
    private Integer productId;

    /**
     * 货品ID
     */
    private Integer productGoodsId;

    /**
     * 库存
     */
    private Integer productStock;

    /**
     * 活动类型
     * @see ActTypeEnum
     */
    private Integer type;

    private Date createTime;

    private Date updateTime;

    /**
     * 实际销量
     */
    private Integer actualSales;

    /**
     * 销量(含取消)
     */
    private Integer sales;

    private String appOwnerCode;

}
