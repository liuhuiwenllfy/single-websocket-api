package cn.liulingfengyu.websocket.core.publish;

import cn.hutool.json.JSONUtil;
import cn.liulingfengyu.websocket.bo.MessageBo;
import cn.liulingfengyu.websocket.core.conf.WebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 订阅监听配置
 *
 * @author LLFY
 */
@Slf4j
@Component
public class DefaultMessageSubListener extends MessageSubListener {

    @Override
    public void onMessage() {
        String channel = super.getChannel();
        String msg = super.getMsg();
        if (channel.equals(ConstantConfiguration.WEBSOCKET)) {
            log.info("redis订阅：主题->{}，消息->{}", channel, msg);
            WebSocketHandler webSocketHandler = new WebSocketHandler();
            MessageBo messageBo = JSONUtil.toBean(JSONUtil.toJsonStr(msg), MessageBo.class);
            webSocketHandler.sendMessage(messageBo);
        }
    }

}
