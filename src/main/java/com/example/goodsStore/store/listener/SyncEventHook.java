package com.example.goodsStore.store.listener;

/**
 * @author yangchen
 * @date 2019-10-17 10:48
 */
public interface SyncEventHook<T> {
    /**
     * 增加监听者
     * @param event
     */
    void addListener(EventSource<T> event);
}
