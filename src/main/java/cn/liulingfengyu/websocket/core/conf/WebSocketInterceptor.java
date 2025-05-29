package cn.liulingfengyu.websocket.core.conf;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * websocket连接前置和连接关闭监听
 *
 * @author LLFY
 */
@Slf4j
public class WebSocketInterceptor implements HandshakeInterceptor {

    private static final String USER_ID = "userId";

    @Override
    public boolean beforeHandshake(@Nonnull ServerHttpRequest serverHttpRequest,
                                   @Nonnull ServerHttpResponse serverHttpResponse,
                                   @Nonnull WebSocketHandler webSocketHandler,
                                   @Nonnull Map<String, Object> map) {
        if (serverHttpRequest instanceof ServletServerHttpRequest servletServerHttpRequest) {
            map.put(USER_ID, servletServerHttpRequest.getServletRequest().getParameter(USER_ID).concat("-").concat(UUID.randomUUID().toString(true)));
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(@Nonnull ServerHttpRequest serverHttpRequest,
                               @Nonnull ServerHttpResponse serverHttpResponse,
                               @Nonnull WebSocketHandler webSocketHandler, Exception e) {
        if (serverHttpRequest instanceof ServletServerHttpRequest servletServerHttpRequest) {
            String userId = servletServerHttpRequest.getServletRequest().getParameter(USER_ID);
            log.info("用户{}链接成功", userId);
        }
    }
}
