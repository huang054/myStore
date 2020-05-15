package com.example.goodsStore.store.request;


import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Data
@Builder
public class StockWrapper {

    /**
     * 操作id，唯一标识
     */
    private String actionId;

    /**
     * 执行正常的业务流程，比如是加减库存操作
     */
    private Function<Command, Long> action;
    /**
     * 回滚库存操作
     */
    private Function<Command, Long> rollback;

    /**
     * 库存常用
     */
    private List<CommandParam> commandParams;

    /**
     * 增加库存
     */
    private boolean add;


}
