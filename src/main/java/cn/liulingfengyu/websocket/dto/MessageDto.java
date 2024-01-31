package cn.liulingfengyu.websocket.dto;

import lombok.Data;

import java.util.List;

/**
 * 消息模型
 *
 * @author LLFY
 */
@Data
public class MessageDto {

    /**
     * 用户id集合
     */
    private List<String> userIdList;
    /**
     * 消息类型
     */
    private String type;
    /**
     * 其他信息
     */
    private String message;

}
