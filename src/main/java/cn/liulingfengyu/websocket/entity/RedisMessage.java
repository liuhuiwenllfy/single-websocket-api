package cn.liulingfengyu.websocket.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RedisMessage {

    /**
     * 全局唯一id
     */
    private String uuid;

    /**
     * 其他信息
     */
    private Message message;
}
