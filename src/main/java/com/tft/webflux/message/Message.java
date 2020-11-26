package com.tft.webflux.message;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.Instant;

/**
 * @ClassName : Message
 * @Description : 请求消息体
 * @Author : Luoj
 * @Date: 2020-11-24 18:30
 */
@Setter
@Getter
@Accessors(chain = true)
@ToString
public class Message<T> {
    private String userId;
    private Status status = Status.open;
    private long createTime = Instant.now().toEpochMilli();
    private T data;

    public enum Status {
        open, close;
    }
}
