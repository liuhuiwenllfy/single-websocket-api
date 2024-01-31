package cn.liulingfengyu.websocket.core.conf;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.liulingfengyu.websocket.bo.MessageBo;
import cn.liulingfengyu.websocket.bo.RedisMessageBo;
import cn.liulingfengyu.websocket.core.publish.ConstantConfiguration;
import cn.liulingfengyu.websocket.dto.MessageDto;
import cn.liulingfengyu.websocket.utils.GetBeanUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WebSocket拦截器
 *
 * @author LLFY
 */
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
    private final StringRedisTemplate stringRedisTemplate = GetBeanUtils.getBean(StringRedisTemplate.class);

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
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        try {
            JSONObject jsonObject = JSONUtil.parseObj(message.getPayload().toString());
            MessageBo messageBo = JSONUtil.toBean(jsonObject, MessageBo.class);
            List<String> userIdList = USERID_MAP.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(session.getId()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            messageBo.setUserIdList(userIdList);
            Map<String, String> map = new HashMap<>();
            map.put("type", messageBo.getType());
            map.put("message", messageBo.getMessage());
            messageBo.setMessage(JSONUtil.toJsonStr(map));
            sendMessage(messageBo);
        } catch (Exception e) {
            log.error("未按照指定数据类型发送信息", e);
        }
    }

    /**
     * 连接出错会调用
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        SESSION_MAP.remove(session.getId());
        USERID_MAP.values().remove(session.getId());
    }

    /**
     * 连接关闭会调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
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
     * @param messageDto 消息模型
     */
    public void sendMessage(MessageDto messageDto) {
        MessageBo messageBo = new MessageBo();
        BeanUtils.copyProperties(messageDto, messageBo);
        Map<String, String> map = new HashMap<>();
        map.put("type", messageBo.getType());
        map.put("message", messageBo.getMessage());
        messageBo.setMessage(JSONUtil.toJsonStr(map));
        sendMessage(messageBo);
    }

    /**
     * 后端发送消息
     */
    public void sendMessage(MessageBo messageBo) {
        if (messageBo.isPublish()) {
            //发送消息需要通过redis发布出去
            publish(messageBo);
        } else if (messageBo.getUserIdList() == null || messageBo.getUserIdList().isEmpty()) {
            //未指定用户，发送给所有在线的客户
            USERID_MAP.forEach((id, sessionId) -> send(sessionId, messageBo.getMessage()));
        } else {
            //根据指定客户筛选出客户对应的sessionId集合
            List<String> sessionIdList = USERID_MAP.entrySet().stream()
                    .filter(entry -> messageBo.getUserIdList().stream().anyMatch(id -> entry.getKey().split("-")[0].equals(id) || entry.getKey().equals(id)))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
            //发布消息
            sessionIdList.forEach(sessionId -> send(sessionId, messageBo.getMessage()));
        }
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
     * @param messageBo 消息
     */
    private void publish(MessageBo messageBo) {
        messageBo.setPublish(false);
        RedisMessageBo redisMessageBo = new RedisMessageBo();
        redisMessageBo.setUuid(UUID.randomUUID().toString(true));
        redisMessageBo.setMessage(JSONUtil.toJsonStr(messageBo));
        stringRedisTemplate.convertAndSend(ConstantConfiguration.WEBSOCKET, JSONUtil.toJsonStr(redisMessageBo));
    }
}
