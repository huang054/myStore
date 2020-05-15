package com.example.goodsStore.store.service.impl;

import com.example.goodsStore.store.listener.DefaultSyncListener;
import com.example.goodsStore.store.listener.EventSource;
import com.example.goodsStore.store.listener.SyncEventHook;
import com.example.goodsStore.store.listener.SyncEventListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Service
public class StockQueueBiz<T> implements Runnable, SyncEventHook<T> {
    private final SyncEventListener<T> syncEventListener = new DefaultSyncListener<>();

    @Value("${dynamic.update.stock.scheduled.ThreadPool.origin:1}")
    private Integer origin;

    @Value("${dynamic.update.stock.scheduled.ThreadPool.bound:5}")
    private Integer bound;

    @Value("${dynamic.update.stock.thread.pool.executor.delay:300}")
    private Long delay;
    private static final int LOG_COUNT = 100;
    private long count;
    private static final AtomicBoolean SHUTDOWN = new AtomicBoolean(false);
    private static final int WARN_ELAPSED_TIME = 30_000;
    private LinkedBlockingQueue<T> asyncQueue = null;
    private static final ScheduledThreadPoolExecutor THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(
            1);

    @PostConstruct
    public void init() {
        asyncQueue = new LinkedBlockingQueue<>();
        THREAD_POOL_EXECUTOR.schedule(this, delay, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
       //开始关闭异步落库队列线程");
        SHUTDOWN.compareAndSet(false, true);
        flush();
        THREAD_POOL_EXECUTOR.shutdown();
        THREAD_POOL_EXECUTOR.awaitTermination(30, TimeUnit.SECONDS);
       //结束关闭异步落库队列线程");
    }

    public void addQueue(List<T> productGoods) {
        asyncQueue.addAll(productGoods);
    }


    private void flush() {
        List<T> queue = poll(asyncQueue);
        syncEventListener.onNotify(queue);
    }

    @Override
    public void addListener(EventSource<T> event) {
        syncEventListener.addListener(event);
    }

    @Override
    public void run() {
        try {
            //LogTrackerUtil.addLogTracker(LogTrackerUtil.create());
           // log.debug("商品货品库存数据异步扣减执行开始");
            long s = System.currentTimeMillis();

            flush();
            long e = System.currentTimeMillis() - s;

            if (e > WARN_ELAPSED_TIME) {
                //log.warn("异步刷新库存数据耗时：{}ms", e);
            } else if (++count%LOG_COUNT == 0){
               // log.info("异步刷新库存数据耗时：{}ms", e);
            }
        } catch (Exception e) {
           // log.error("商品货品库存数据异步扣减执行失败，异常信息:", e);
        } finally {
            if (SHUTDOWN.get()) {
                //log.info("异步落库定时任务已经关闭，不用再继续调度任务");
            } else {
                THREAD_POOL_EXECUTOR.schedule(this, ThreadLocalRandom.current().nextInt(origin, bound), TimeUnit.SECONDS);
            }
          //  LogTrackerUtil.removeTracker();
        }
    }

    private List<T> poll(LinkedBlockingQueue<T> queue) {
        if (queue.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> newer = new ArrayList<>(queue.size() * 3 / 2);
        T item;
        while ((item = queue.poll()) != null) {
            newer.add(item);
        }
        return newer;
    }

}
