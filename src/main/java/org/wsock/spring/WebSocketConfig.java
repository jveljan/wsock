package org.wsock.spring;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.wsock.internal.SessionAcceptInterceptor;
import org.wsock.internal.WsockServiceImpl;
import org.wsock.pub.WsockConfig;
import org.wsock.pub.WebSocket;
import org.wsock.pub.WsockInit;
import org.wsock.internal.WsockHandler;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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
        definitions.forEach(wsInit -> {
            WebSocket wsAnnotation = wsInit.getClass().getAnnotation(WebSocket.class);
            WsockHandler connections = new WsockHandler(objectMapper);
            WsockServiceImpl wsockService = new WsockServiceImpl(connections);
            SpringSoHandler handler = new SpringSoHandler(connections);

            wsInit.init(wsockService);

            WsockConfig cfg = new WsockConfig();
            Consumer<WsockConfig> configurator = wsockService.getConfigurator();
            if(configurator != null) {
                configurator.accept(cfg);
            }

            WebSocketHandlerRegistration registration = registry.addHandler(handler, wsAnnotation.value());

            Function<String, Boolean> tokenAcceptor = wsockService.getTokenAcceptor();
            if(tokenAcceptor != null) {
                SessionAcceptInterceptor interceptor = new SessionAcceptInterceptor(tokenAcceptor);
                registration.addInterceptors(interceptor);
            }
            if(cfg.getAllowedOrigins() != null) {
                registration.setAllowedOrigins(cfg.getAllowedOrigins());
            }
            log.debug("Registered WebSocket for {} on path={}", wsInit, wsAnnotation.value());
        });
    }
}