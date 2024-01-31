package cn.liulingfengyu.websocket.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author liuhuiwen
 */
@AllArgsConstructor
@Getter
public enum NewsEnum {

    /**
     * 初次连接
     */
    LOGIN("LOGIN", "连接websocket"),
    ;

    private final String code;

    private final String message;
}
