package com.example.goodsStore.store.repository;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;

import com.example.goodsStore.store.RedisConstant;
import com.example.goodsStore.store.RedisContext;
import com.example.goodsStore.store.request.*;
import com.example.goodsStore.store.service.impl.StockQueueBiz;
import com.example.goodsStore.store.utils.*;
import io.lettuce.core.KeyValue;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Repository
public class StoreRepository {


    /**
     * @see// GoodsStockUtil#assemblyActivityStockKeyWithoutPrefix(Integer, Integer, Integer, String)
     */
    @CreateCache(
            area = RedisConstant.Area.STOCK,
            name = RedisConstant.Key.PRODUCT_GOODS_PRE,
            cacheType = CacheType.REMOTE)
    private Cache<String, Object> productGoodsStockCache;
    @Autowired
    private StockQueueBiz stockQueueBiz;
    private final GenericObjectPool<StatefulRedisConnection<String, String>> redisPool;


    @Autowired
    public StoreRepository(GenericObjectPool<StatefulRedisConnection<String, String>> redisPool) {
        this.redisPool = redisPool;
    }

    public void addStore(StoreRequest request){
        addStock(getGoodsStockWrapper(request));
    }

    private void addStock(StockWrapper goodsStockWrapper) {
        commonGoodsStock(goodsStockWrapper,true);
    }

    public void subStore(StoreRequest request){
        subStock(getGoodsStockWrapper(request));
    }

    private void subStock(StockWrapper goodsStockWrapper) {
        commonGoodsStock(goodsStockWrapper,false);
    }

    void commonGoodsStock(StockWrapper requests,boolean addStock){
        StockWrapper wrapper = StockWrapper.builder()
                .actionId(LettuceUtils.idempotentMaker(ActionIdUtil.setActionId(), addStock))

                .action(v -> addStock?v.getCommand().incrby(v.getKey(), v.getValue())
                        :v.getCommand().decrby(v.getKey(), v.getValue()))
                .rollback(v -> addStock?v.getCommand().decrby(v.getKey(), v.getValue())
                        :v.getCommand().incrby(v.getKey(), v.getValue()))
                .commandParams(requests.getCommandParams())
                .add(addStock)

                .build();
        operateStock(wrapper);
    }

    /**
     * 加/减库存(包括普通商品库存和活动商品库存等，本质上只是在库存key前缀不一致)
     */
    private void operateStock(StockWrapper wrapper) {
        //远端提交操作库存记录
        boolean suc = LettuceUtils.tryLock(redisPool, wrapper.getActionId(),
                TimeUnit.MINUTES.toSeconds(10));
        if (!suc) {
            //"重复提交操作库存请求:actionId
            return;
        }
        // 执行初始化数据操作
        lazyInit(wrapper.getCommandParams());

        //错误消息
        String errMsg = null;
        boolean rollbackFlag = false;
        //todo 日志落库恢复问题库存
       //开始操作货品库存
        try (StatefulRedisConnection<String, String> connect = redisPool.borrowObject()) {
            RedisCommands<String, String> sync = connect.sync();
            for (CommandParam internal : wrapper.getCommandParams()) {
                String key = RedisContext.generatorCacheKey(internal.getStoreId().toString());
                // 构建交由redis执行的命令参数
                  Command command = new Command(sync, key,
                          GoodsAndSalesUtil.generateStockAndSalesFactor(internal.getStockNum(), true));
                //单项开始操作redis库存,values:{}", internal
                Long result = wrapper.getAction().apply(command);
                //单项结束操作redis库存和销量,stock:{},sales:{},result:{}",
                //fetchProductStock(result), fetchProductSales(result), result);
                internal.setAction(true);
                internal.setResult(result);
                if (GoodsAndSalesUtil.fetchProductStock(result) < 0) {
                    rollbackFlag = true;
                    //扣减/增加库存操作失败：{}", key);
                    //errMsg = LuaStockErrorEnum.UNDER_STOCK_CODE.getDesc();
                    break;
                } else if (GoodsAndSalesUtil.fetchProductSales(result) < 0) {
                  //出现负数销量,key:{},value:{},sales:{}", key, result, fetchProductSales(result));
                }
            }

            if (rollbackFlag) {
                for (CommandParam internal : wrapper.getCommandParams()) {
                    if (!internal.isAction()) {
                        continue;
                    }
                    String key = RedisContext.generatorCacheKey(internal.getStoreId().toString());
                   //单项开始回滚redis库存,goods_id:{},num:{}", key, internal.getStockNum());
                    Command command = new Command(sync, key,
                            GoodsAndSalesUtil.generateStockAndSalesFactor(internal.getStockNum(), true));
                    Long result = wrapper.getRollback().apply(command);
                  //单项结束回滚redis库存和销量,stock:{},sales:{}",
                    //        fetchProductStock(result), fetchProductSales(result));
                }
            } else {
                // 更新防重key过期时间
                LettuceUtils.expireIdempotent(sync, wrapper.getActionId(), TimeUnit.MINUTES.toSeconds(10));
            }
        } catch (Exception e) {
          //"库存操作异常,", e);
            throw new RuntimeException("库存操作异常");
        }
        //结束操作货品库存");
        if (errMsg != null) {
            throw new RuntimeException("StockErrorMsgEnum.UNDER_STOCK.code(), errMsg");
        }
        // 把数据写入到队列，供消费者做后置处理
        List<ProductGoodsDTO> goods = wrapper.getCommandParams()
                .stream().map(s -> ProductGoodsDTO.builder()
                        .operationId(wrapper.getActionId())

                        .productId(s.getStoreId())


                        .productStock(wrapper.isAdd() ? s.getStockNum() : (-1 * s.getStockNum()))
                        .actualSales(wrapper.isAdd() ? (-1 * s.getStockNum()) : s.getStockNum())
                        .sales(wrapper.isAdd() ? (-1 * s.getStockNum()) : s.getStockNum())
                        .productSalesNum(wrapper.isAdd() ? (-1 * s.getStockNum()) : s.getStockNum())

                        .add(wrapper.isAdd())
                        .common(true)
                        .build())
                .collect(Collectors.toList());
        stockQueueBiz.addQueue(goods);
    }

    void lazyInit(List<CommandParam> internals) {
        Set<String> productGoodKeys = internals.stream()
                .map(p->RedisContext.generatorCacheKey(p.getStoreId().toString()))
                .collect(Collectors.toSet());
        new LoadDataFactory().lazeInit(productGoodsStockCache, productGoodKeys);
    }


    public StockWrapper getGoodsStockWrapper(StoreRequest requests){
       StockWrapper wrapper = StockWrapper.builder()

                .commandParams(BeanCopyUtils.copyList(CommandParam.class, requests.getStoreInfoList()))
                
                .build();

        return wrapper;
    }

    public void rollBackStore(StoreRequest request){

        try (StatefulRedisConnection<String, String> connection = redisPool.borrowObject()) {
            RedisCommands<String, String> sync = connection.sync();
            //获取增减Key
            List<KeyValue<String, String>> results = sync.mget(
                    LettuceUtils.idempotentMaker(ActionIdUtil.setActionId(), true),
                    LettuceUtils.idempotentMaker(ActionIdUtil.setActionId(), false));
            String addResult = results.get(0).getValueOrElse("");
            String subResult = results.get(1).getValueOrElse("");

            //已经成对,或者都没有值则不操作
            if (!addResult.isEmpty() && subResult.isEmpty()) {
                subStore(request);
            } else if (addResult.isEmpty() && !subResult.isEmpty()) {
                addStore(request);
            }

        } catch (Exception e) {

            throw new RuntimeException("回滚库存异常");
        }
    }
}
