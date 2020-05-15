package com.example.goodsStore.store.request;

import lombok.Data;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Data
public class CommandParam {

    /**
     * 库存Id
     */
    private Integer storeId;
    /**
     * 添加库存的数量
     */
    private Integer stockNum;
    /**
     * 库存结果
     */
    private Long result;
    /**
     * 是否执行过，用于判断是否需要回滚
     */
    private boolean action;
}
