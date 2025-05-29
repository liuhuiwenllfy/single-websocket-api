package cn.liulingfengyu.websocket.core.conf;

import cn.hutool.core.lang.UUID;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import cn.liulingfengyu.websocket.core.publish.ConstantConfiguration;
import cn.liulingfengyu.websocket.entity.Message;
import cn.liulingfengyu.websocket.entity.RedisMessage;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket拦截器
 *
 * @author LLFY
 */
@Component
@Slf4j
public class WebSocketHandler extends AbstractWebSocketHandler {
    /**
     * 存储sessionId和webSocketSession
     * 需要注意的是，webSocketSession没有提供无参构造，不能进行序列化，也就不能通过redis存储
     * 在分布式系统中，要想别的办法实现webSocketSession共享（解决方案：采用redis发布订阅功能实现）
     * 我们自定义消息模型，消息格式为{"userIdList":['用户名称'],"type":"类型","message":"你要发送的消息"}
     */
    private static final Map<String, WebSocketSession> SESSION_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> USERID_MAP = new ConcurrentHashMap<>();
    private static final String USER_ID = "userId";
    private final StringRedisTemplate stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);

    /**
     * webSocket连接创建后调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        USERID_MAP.put(String.valueOf(session.getAttributes().get(USER_ID)), session.getId());
        SESSION_MAP.put(session.getId(), session);
    }

    /**
     * 接收到消息会调用
     */
    @Override
    public void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) {
        try {
            sendMessage(JSONUtil.toBean(message.getPayload().toString(), Message.class));
        } catch (Exception e) {
            log.error("未按照指定数据类型发送信息", e);
        }
    }

    /**
     * 连接出错会调用
     */
    @Override
    public void handleTransportError(WebSocketSession session, @NonNull Throwable exception) {
        SESSION_MAP.remove(session.getId());
        USERID_MAP.values().remove(session.getId());
    }

    /**
     * 连接关闭会调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        SESSION_MAP.remove(session.getId());
        USERID_MAP.values().remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 其他模块调用该方法
     *
     * @param message 消息模型
     */
    /**
     * 发送消息给指定用户或广播
     *
     * @param message 消息模型
     */
    public void sendMessage(Message message) {
        if (message == null) {
            log.warn("消息不能为空");
            return;
        }
        if (message.isPublish()) {
            publish(message);
            return;
        }
        List<String> userIdList = message.getUserIdList();
        String msgContent = message.getMessage();

        if (userIdList == null || userIdList.isEmpty()) {
            // 广播给所有在线用户
            USERID_MAP.forEach((id, sessionId) -> send(sessionId, msgContent));
            return;
        }

        // 根据目标用户筛选出对应的 sessionId 列表
        List<String> sessionIdList = USERID_MAP.entrySet().stream().filter(entry -> userIdList.stream().anyMatch(id -> entry.getKey().split("-")[0].equals(id))).map(Map.Entry::getValue).toList();

        // 向匹配的会话发送消息
        sessionIdList.forEach(sessionId -> send(sessionId, msgContent));
    }


    /**
     * 指定用户发送消息
     *
     * @param sessionId 消息
     */
    @SneakyThrows
    private void send(String sessionId, String msg) {
        WebSocketSession session = SESSION_MAP.get(sessionId);
        if (session != null) {
            session.sendMessage(new TextMessage(msg));
        }
    }

    /**
     * redis发布
     *
     * @param message 消息
     */
    private void publish(Message message) {
        message.setPublish(false);
        RedisMessage redisMessageBo = new RedisMessage();
        redisMessageBo.setUuid(UUID.randomUUID().toString(true));
        redisMessageBo.setMessage(message);
        stringRedisTemplate.convertAndSend(ConstantConfiguration.WEBSOCKET, JSONUtil.toJsonStr(redisMessageBo));
    }
}
