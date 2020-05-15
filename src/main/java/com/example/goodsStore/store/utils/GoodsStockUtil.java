package com.example.goodsStore.store.utils;



import com.example.goodsStore.store.ActTypeEnum;
import com.example.goodsStore.store.RedisConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * @author yangchen.huang
 */
public final class GoodsStockUtil {

    public static final String NORMAL_STOCK = "";

    /**
     * 拼接普通库存key
     *
     * @param productId
     * @param goodsId
     * @return
     */
    public static String assemblyNormalStockKey(Integer productId, Integer goodsId) {
        return assemblyActivityStockKey(productId, goodsId, ActTypeEnum.ACT_NORMAL_CODE.getCode(), null);
    }

    /**
     * 拼接活动缓存key，在普通商品key基础下再追加一个活动ID,保持兼容普通商品数据
     * 如果是普通商品则返回{@link #assemblyNormalStockKey(Integer, Integer)}
     */
    public static String assemblyActivityStockKey(
            Integer productId, Integer goodsId, Integer type, String activityId) {
        return appendGoodsKeyPrefix(
                assemblyActivityStockKeyWithoutPrefix(productId, goodsId, type, activityId));
    }

    /**
     * 为了兼容旧版本获取库存是不需要传入类型的
     *
     * @param type
     * @return
     */
    public static int convertType(Integer type) {
        return type == null ? ActTypeEnum.ACT_NORMAL_CODE.getCode() : type;
    }

    public static String convertActivityId(String activity) {
        return activity == null ? NORMAL_STOCK : activity;
    }

    public static Integer convertCurrentUserId(String currentUserId) {
        return StringUtils.isEmpty(currentUserId) ? 0 : Integer.valueOf(currentUserId);
    }

    public static String assemblyActivityStockKeyWithoutPrefix(
            Integer productId, Integer goodsId, Integer type, String activityId) {
        Objects.requireNonNull(productId, "商品ID不能为空");
        Objects.requireNonNull(goodsId, "货品ID不能为空");
        String stockKey = String.format(RedisConstant.Key.GOODS_STOCK_VALUE, convertType(type), productId, goodsId);
        if (!StringUtils.isEmpty(activityId)) {
            if (type == null || ActTypeEnum.ACT_NORMAL_CODE.getCode().equals(type)) {
                throw new RuntimeException("组合普通商品库存key时不能传入活动ID");
            }
            stockKey += ":" + activityId;
        }
        return stockKey;
    }


    /**
     * 由于使用jetCache，jetCache默认会将key带上前缀，而使用redisClient又没有前缀
     * 所以在使用redisClient时需要把前缀补上
     * 解析redis货品key
     *
     * @see #assemblyActivityStockKey(Integer, Integer, Integer, String)
     */
    public static String appendGoodsKeyPrefix(String key) {
        return RedisConstant.Key.PRODUCT_GOODS_PRE + key;
    }

    /**
     * product:goods:stock:0:1:{2} or product:goods:stock:0:1:{2}:11223344
     * or
     * 0:1:{2} or 0:1:{2}:11223344
     *
     * @param key
     * @return
     */
    public static StockCacheKey generatorStockCacheKey(String key) {
        Objects.requireNonNull(key);
        String[] v = StringUtils.delimitedListToStringArray(key, ":");
        if (key.startsWith(RedisConstant.Key.PRODUCT_GOODS_PRE)) {
            return StockCacheKey.builder()
                    .type(Integer.valueOf(v[3]))
                    .productId(Integer.valueOf(v[4]))
                    .productGoodsId(Integer.valueOf(v[5]))
                    .activityId(v.length == 7 ? v[6] : GoodsStockUtil.NORMAL_STOCK)
                    .build();
        }
        return StockCacheKey.builder()
                .type(Integer.valueOf(v[0]))
                .productId(Integer.valueOf(v[1]))
                .productGoodsId(Integer.valueOf(v[2]))
                .activityId(v.length == 4 ? v[3] :GoodsStockUtil.NORMAL_STOCK)
                .build();
    }

    /**
     * {@link RedisConstant.Key#AGGREGATION_SALES_PRE}
     * product:sales:aggregation:19991
     *
     * @param key
     * @return
     */
    public static Integer fetchProductIdFromAggregationSalesKey(String key) {
        Objects.requireNonNull(key);
        return Integer.valueOf(key.split(":")[3]);
    }

    public static String generatorAggregationSalesKey(Integer productId) {
        Objects.requireNonNull(productId);
        return RedisConstant.Key.AGGREGATION_SALES_PRE + productId;
    }

    public static boolean illegalStock(Object o) {
        return StringUtils.isEmpty(o) ;
    }

    public static long toNumber(Object o) {
        if (illegalStock(o)) {
            return 0;
        }
        return Long.valueOf(String.valueOf(o));
    }

    public static int zeroBackup(Integer integer) {
        return integer == null ? 0 : integer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StockCacheKey {
        private Integer productId;
        private Integer productGoodsId;
        private Integer type;
        private String activityId;
    }
}
