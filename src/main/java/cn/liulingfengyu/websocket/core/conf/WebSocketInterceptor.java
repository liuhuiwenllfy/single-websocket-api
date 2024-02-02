package cn.liulingfengyu.websocket.core.conf;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
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
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
            String token = UDecoder.URLDecode(servletServerHttpRequest.getServletRequest().getParameter(TOKEN), Charset.defaultCharset());
            if (StrUtil.isBlank(token)) {
                // TODO liuhuiwen 2024/1/31：验证token是否有效
                throw new Exception("未登录");
            }
            map.put(USER_ID, servletServerHttpRequest.getServletRequest().getParameter(USER_ID).concat(UUID.randomUUID().toString(true)));
            return true;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
    }
}
