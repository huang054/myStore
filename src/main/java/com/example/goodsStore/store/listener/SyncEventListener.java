package com.example.goodsStore.store.listener;

import java.util.EventListener;
import java.util.List;

/**
 * @author yangchen
 * @date 2019-10-17 10:14
 */
public interface SyncEventListener<T> extends EventListener, SyncEventHook<T> {
    /**
     * 触发事件
     * @param t
     */
    void onNotify(List<T> t);
}
