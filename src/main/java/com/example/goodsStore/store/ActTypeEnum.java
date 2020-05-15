package com.example.goodsStore.store;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
public enum ActTypeEnum {

    ACT_NORMAL_CODE(0, "普通商品"),
    ACT_HOTSHOT_CODE(1, "大咖"),
    ACT_SECONDS_KILL_CODE(2, "秒杀"),
    ACT_COLLAGE_CODE(3, "拼团"),
    ACT_FADDISH_CODE(4, "爆款"),
    ACT_SELLFAST_CODE(5, "卖快")
    ;




    private Integer code;
    private String desc;

    ActTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }
}
