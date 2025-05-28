package cn.liulingfengyu.websocket.controller;

import cn.liulingfengyu.websocket.core.conf.WebSocketHandler;
import cn.liulingfengyu.websocket.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private WebSocketHandler webSocketHandler;

    /**
     * 发送消息
     *
     * @param message 输入参数
     * @return {@link Boolean}
     */
    @PostMapping("sendMessage")
    public Boolean sendMessage(@RequestBody @Validated Message message) {
        webSocketHandler.sendMessage(message);
        return true;
    }
}
