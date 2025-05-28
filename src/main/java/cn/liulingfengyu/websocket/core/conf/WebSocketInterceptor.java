package cn.liulingfengyu.websocket.core.conf;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Nonnull;
import org.apache.tomcat.util.buf.UDecoder;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * websocket连接前置和连接关闭监听
 *
 * @author LLFY
 */
public class WebSocketInterceptor implements HandshakeInterceptor {

    private static final String USER_ID = "userId";

    private static final String TOKEN = "token";

    @Override
    public boolean beforeHandshake(@Nonnull ServerHttpRequest request,
                                   @Nonnull ServerHttpResponse serverHttpResponse,
                                   @Nonnull WebSocketHandler webSocketHandler,
                                   @Nonnull Map<String, Object> map) throws Exception {
        if (request instanceof ServletServerHttpRequest servletServerHttpRequest) {
            String token = UDecoder.URLDecode(servletServerHttpRequest.getServletRequest().getParameter(TOKEN), Charset.defaultCharset());
            if (StrUtil.isBlank(token)) {
                throw new Exception("未登录");
            }
            map.put(USER_ID, servletServerHttpRequest.getServletRequest().getParameter(USER_ID).concat("-").concat(UUID.randomUUID().toString(true)));
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(@Nonnull ServerHttpRequest serverHttpRequest,
                               @Nonnull ServerHttpResponse serverHttpResponse,
                               @Nonnull WebSocketHandler webSocketHandler, Exception e) {
    }
}
