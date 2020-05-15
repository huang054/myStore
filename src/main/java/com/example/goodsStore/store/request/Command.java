package com.example.goodsStore.store.request;

import io.lettuce.core.api.sync.RedisCommands;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HuangJ
 * @Date 2020/5/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Command {

    private RedisCommands<String, String> command;
    private String key;
    private long value;
}
