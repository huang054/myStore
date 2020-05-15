package com.example.goodsStore.store;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
public final class RedisConstant {

    public static final String DYNAMIC_UPDATE_PREFIX = "dynamic.update";
    /**
     * 统一 empty 过期时间 防止 雪崩效应
     * 单位：秒
     */
    public static final Integer EMPTY_EXPIRE_TIME = 120;


    /**
     * 统一 懒加载 分布式锁 过期时间
     */
    public static final Integer LOCK_EXPIRE_TIME = 120;

    /**
     * 不回滚
     */
    public static final Integer IS_NOT_ROLLBACK = 0;

    /**
     * jetcache 区域配置
     */
    public static final class Area {

        /**
         * 库存
         */
        public static final String STOCK = "stock";

        /**
         * 累计热销
         */
        public static final String SALES = "sales";
    }

    /**
     * 定义Redis 中Key
     */
    public static final class Key {
        public static final String CLEAR_REDIS_STOCK_KEY = "clear:redis:stock:key";
        public static final String STOCK_LOG_PRE = "stock:onlyStock:log:";
        public static final String STOCK_IDEMPOTENT_PRE = "stock:tryLock:";
        public static final String VIRTUAL_SALES_IDEMPOTENT_PRE = "virtual:sales:tryLock:";
        public static final String STOCK_IDEMPOTENT_QUEUE = "stock:tryLock:queue";
        public static final String STOCK_INIT_IDEMPOTENT_PRE = "stock:init:tryLock:";
        public static final String STOCK_IDEMPOTENT_ADD_SUF = ":add";
        public static final String STOCK_IDEMPOTENT_SUB_SUF = ":sub";
        public static final String STOCK_IDEMPOTENT_ADJUST_SUF = ":adjust";
        public static final String STOCK_IDEMPOTENT_COMPENSATION_SUF = ":compensation";
        public static final String STOCK_IDEMPOTENT_STARY_ACT = ":start:act";
        public static final String STOCK_IDEMPOTENT_END_ACT = ":end:act";
        public static final String STOCK_PRODUCT_GOODS_SYNC_ID = "stock:product:goods:sync:id";
        public static final String STOCK_PRODUCT_GOODS_SYNC_LOCK = "stock:product:goods:sync:lock";

        /**
         * 货品前缀
         */
        public static final String PRODUCT_GOODS_PRE = "product:goods:stock:";

        /**
         * 累计热销前缀
         */
        public static final String AGGREGATION_SALES_PRE = "product:sales:aggregation:";

        /**
         * 货品实际存储的值，以货品ID为redis hashTag
         */
        public static final String GOODS_STOCK_VALUE = "%s:%s:%s";

        /**
         * 货品库存key
         * product:goods:stock:type:product_id:goods_id
         * product:goods:stock:type:product_id:goods_id:activity_id
         */
        public static final String GOODS_STOCK_KEY = PRODUCT_GOODS_PRE + GOODS_STOCK_VALUE;

        /**
         * 锁后缀
         */
        public static final String LOCK_SUF = ":lock";

    }

}
