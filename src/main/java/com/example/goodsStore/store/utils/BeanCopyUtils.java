package com.example.goodsStore.store.utils;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cglib.beans.BeanCopier;

import org.springframework.cglib.core.Converter;
import org.springframework.util.CollectionUtils;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
public class BeanCopyUtils {
    private static final Logger log = LoggerFactory.getLogger(BeanCopyUtils.class);

    public BeanCopyUtils() {
    }

    public static <T> List<T> copyList(Class<T> dest, List orig) {
        if (CollectionUtils.isEmpty(orig)) {
            return new ArrayList();
        } else {
            BeanCopier beanCopier = BeanCopier.create(orig.get(0).getClass(), dest, false);
            ArrayList resultList = new ArrayList(orig.size());

            try {
                Iterator var4 = orig.iterator();

                while(var4.hasNext()) {
                    Object o = var4.next();
                    if (o != null) {
                        T destObject = dest.newInstance();
                        beanCopier.copy(o, destObject, (Converter)null);
                        resultList.add(destObject);
                    }
                }

                return resultList;
            } catch (Exception var7) {
                log.error("copyList error", var7);
                return resultList;
            }
        }
    }

}
