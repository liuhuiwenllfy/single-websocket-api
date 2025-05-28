package cn.liulingfengyu.websocket.core.publish;

import cn.hutool.json.JSONUtil;
import cn.liulingfengyu.websocket.core.conf.WebSocketHandler;
import cn.liulingfengyu.websocket.entity.RedisMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * 订阅监听配置
 *
 * @author LLFY
 */
@Slf4j
@Component
public class MessageSubListener implements MessageListener {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private WebSocketHandler webSocketHandler;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String channel = new String(bytes);
        RedisMessage redisMessage = JSONUtil.toBean(message.toString(), RedisMessage.class);
        String key = channel.concat(":").concat(redisMessage.getUuid());
        if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, JSONUtil.toJsonStr(redisMessage.getMessage()), 1, TimeUnit.DAYS))) {
            log.info("redis订阅：主题->{}，消息->{}", channel, JSONUtil.toJsonStr(redisMessage.getMessage()));
            webSocketHandler.sendMessage(redisMessage.getMessage());
        } else {
            log.warn("redis订阅：主题->{}，消息->{}，该消息已被消费", channel, JSONUtil.toJsonStr(redisMessage.getMessage()));
        }
    }
}
