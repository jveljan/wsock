package org.wsock.spring;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.wsock.internal.WsockServiceImpl;
import org.wsock.pub.WsockService;
import org.wsock.pub.WebSocket;
import org.wsock.pub.WsockInit;
import org.wsock.internal.SoConnections;

import java.util.List;

@Configuration
@EnableWebSocket
@ConditionalOnBean(WsockInit.class)
public class WebSocketConfig implements WebSocketConfigurer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private List<WsockInit> definitions;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        definitions.forEach(c -> {
            WebSocket wsAnnotation = c.getClass().getAnnotation(WebSocket.class);
            log.debug("Registering WebSocket for {} on path={}", c, wsAnnotation.value());
            SoConnections connections = new SoConnections(objectMapper);
            WsockService wsockService = new WsockServiceImpl(connections);
            SpringSoHandler handler = new SpringSoHandler(connections);

            c.init(wsockService);

            registry.addHandler(handler, wsAnnotation.value())
                    .setAllowedOrigins("*");

        });
    }
}