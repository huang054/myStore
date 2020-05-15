package com.example.goodsStore.store.utils;

/**
 * <p>库存与销量内部存储方式介绍</p>
 * <p>由于库存与销量是同时存在的两个东西，扣减库存则增加销量，增加销量则扣减库存。按照常规的方式，需要存储两个key，而且
 *   为了保证原子性，必须通过lua脚本，否则极有可能导致数据不一致。现在将long类型改为两个int类型，高32位存储销量，低32
 *   位存储库存，这样只要只要一个操作即可控制两个值；但是由于销量与库存是两个反向的结果，为了保证正向操作，需要把销量
 *   再次取反，即增加库存也增加销量，在实际获取的时候，通过一个魔术值扣减存储的销量值得到真实的销量值</p>
 * <table BORDER CELLPADDING=3 CELLSPACING=1>
 *    <caption>销量与魔术关系</caption>
 *    <tr>
 *        <td>魔术</td>
 *        <td>真实销量</td>
 *        <td>redis存储值</td>
 *    </tr>
 *    <tr>
 *        <td>9999</td>
 *        <td>1</td>
 *        <td>9998</td>
 *    </tr>
 *    <tr>
 *        <td>9999</td>
 *        <td>8888</td>
 *        <td>1111</td>
 *    </tr>
 * </table>
 * @author yangchen
 * @date 2019-10-10 20:32
 */
public class GoodsAndSalesUtil {
    private static final long HIGH_MIN = ((long)1 << 32);
    private static final long MAX = HIGH_MIN - 1;
    private static final int SALES_OFFSET = 32;
    private static final int MAGIC_SALES = 999999999;

    /**
     * 低32位只要超过一定值，说明存在向高32位借位的情况，这种情况则表示库存不够
     */
    public static int fetchProductStock(long stock) {
        long low = stock & MAX;
        return (low > MAGIC_SALES) ?  (int)(low - HIGH_MIN) : (int)low;
    }

    public static int fetchProductStock(String stock) {
        return fetchProductStock(Integer.parseInt(stock));
    }

    public static int fetchProductSales(String stock) {
        return fetchProductSales(Integer.parseInt(stock));
    }

    public static int fetchProductSales(long stock) {
        return MAGIC_SALES - (int) (stock >> SALES_OFFSET);
    }

    public static long generateRedisSalesFactor(int sales) {
        return (long)(MAGIC_SALES - sales) << SALES_OFFSET;
    }

    public static long generateStockAndSalesRedisValue(int stock, int sales) {
        return generateRedisSalesFactor(sales) + stock;
    }

    /**
     * 是否增加销量
     * @param origin
     * @param onlyStock
     * @return
     */
    public static long generateStockAndSalesFactor(int origin, boolean onlyStock) {
        return onlyStock ? origin : ((long)origin << SALES_OFFSET) + origin;
    }

    /**
     * 是否增加销量
     * @return
     */
    public static long generateSalesFactor(int origin) {
        return ((long)-origin << SALES_OFFSET);
    }
}
