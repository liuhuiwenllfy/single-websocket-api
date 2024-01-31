package cn.liulingfengyu.websocket.controller;

import cn.liulingfengyu.websocket.core.conf.WebSocketHandler;
import cn.liulingfengyu.websocket.dto.MessageDto;
import cn.liulingfengyu.websocket.utils.RespJson;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 发送消息件控制器
 */
@RestController
@RequestMapping("/websocket")
public class SendMessageController {

    /**
     * 发送消息
     *
     * @param messageDto 输入参数
     * @return {@link RespJson}
     */
    @PostMapping("sendMessage")
    public RespJson<Boolean> sendSimpleTextMailActual(@RequestBody @Validated MessageDto messageDto) {
        WebSocketHandler webSocketHandler = new WebSocketHandler();
        webSocketHandler.sendMessage(messageDto);
        return RespJson.success();
    }
}
