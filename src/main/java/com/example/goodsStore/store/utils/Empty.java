package com.example.goodsStore.store.utils;

import java.io.Serializable;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
public class Empty implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final Empty empty = new Empty();

    public Empty() {
    }

    public static boolean isEmptyType(Object o) {
        boolean r = false;
        if (o == null) {
            return r;
        } else {
            if (Empty.class.isInstance(o)) {
                r = true;
            } else if (Empty.class.isAssignableFrom(o.getClass())) {
                r = true;
            }

            return r;
        }
    }

}
