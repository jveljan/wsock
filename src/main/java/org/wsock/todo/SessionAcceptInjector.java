package org.wsock.todo;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Created by joco on 01.10.16.
 */
class SessionAcceptInjector implements HandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        MultiValueMap<String, String> params = UriComponentsBuilder.fromHttpRequest(serverHttpRequest).build().getQueryParams();
        String token = params.getFirst("token");
        System.out.println("SessionAcceptInjector beforeHandshake ... " + token);
        // TODO: token acceptance service

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

    }
}
