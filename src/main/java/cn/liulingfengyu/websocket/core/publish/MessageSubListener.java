package cn.liulingfengyu.websocket.core.publish;

import cn.hutool.json.JSONUtil;
import cn.liulingfengyu.websocket.bo.RedisMessageBo;
import cn.liulingfengyu.websocket.utils.RedisUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * 订阅监听配置
 *
 * @author LLFY
 */
@Slf4j
@Component
@Getter
@Setter
@Primary
public abstract class MessageSubListener implements MessageListener {

    private volatile String channel;

    private volatile String msg;

    @Autowired
    private RedisUtil redisUtil;

    public abstract void onMessage();

    @Override
    public synchronized void onMessage(Message message, byte[] bytes) {
        channel = new String(bytes);
        RedisMessageBo redisMessageBo = JSONUtil.toBean(message.toString(), RedisMessageBo.class);
        String key = channel.concat(redisMessageBo.getUuid());
        msg = redisMessageBo.getMessage();
        if (!redisUtil.hasKey(key)) {
            redisUtil.setEx(key, redisMessageBo.getMessage(), 3600, TimeUnit.MILLISECONDS);
            onMessage();
        }
    }
}
