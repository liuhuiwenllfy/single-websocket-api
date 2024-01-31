package cn.liulingfengyu.websocket.bo;

import lombok.Data;

import java.util.List;

/**
 * 消息入参
 *
 * @author LLFY
 */
@Data
public class MessageBo {
    /**
     * 是否广播
     */
    private boolean isPublish = true;

    /**
     * 用户id集合
     */
    private List<String> userIdList;
    /**
     * 其他信息
     */
    private String message;
}
