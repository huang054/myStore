package com.example.goodsStore.store.request;

import lombok.Data;

import java.util.List;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Data
public class StoreRequest {


    private Boolean onlyStock;

    private List<StoreInfo> storeInfoList;

    @Data
    public static class StoreInfo{

        private long storeId;

        private int storeNum;
    }
}
