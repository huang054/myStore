package com.example.goodsStore.store.listener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yangchen
 * @date 2019-10-17 10:24
 */
public class DefaultSyncListener<T> implements SyncEventListener<T> {
    private final List<EventSource<T>> listener = new ArrayList<>();
    @Override
    public synchronized void addListener(EventSource<T> event) {
        if (listener.contains(event)) {
            throw new IllegalStateException("duplicate event");
        }
        listener.add(event);
    }

    @Override
    public synchronized void onNotify(List<T> t) {
        listener.forEach(s->s.consumer(t));
    }
}
