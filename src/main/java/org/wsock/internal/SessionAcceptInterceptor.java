package org.wsock.internal;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by joco on 01.10.16.
 */
public class SessionAcceptInterceptor implements HandshakeInterceptor {
    private final Function<String, Boolean> tokenAcceptor;

    public SessionAcceptInterceptor(Function<String, Boolean> tokenAcceptor) {
        this.tokenAcceptor = tokenAcceptor;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        final String token = TokenUtil.extactToken(serverHttpRequest);
        return tokenAcceptor.apply(token);
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

    }
}
