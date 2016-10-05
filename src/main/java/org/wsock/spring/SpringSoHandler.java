package org.wsock.spring;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.wsock.internal.TokenUtil;
import org.wsock.pub.Wsock;
import org.wsock.internal.WsockHandler;
import org.wsock.internal.model.ServerEvent;
import org.wsock.internal.model.WsockEventType;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.wsock.internal.model.WsockEventType.EVENT;


/**
 * Created by joco on 01.10.16.
 */
public class SpringSoHandler extends TextWebSocketHandler {

    private WsockHandler wsockHandler;

    public SpringSoHandler(WsockHandler wsockHandler) {
        this.wsockHandler = wsockHandler;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        final String token = TokenUtil.extactToken(session.getUri());;

        final Wsock wsock = new Wsock() {
            private ConcurrentHashMap<String, Object> sessionData = new ConcurrentHashMap<>();

            @Override
            public String token() {
                return token;
            }

            @Override
            public void send(WsockEventType type, String channel, Object data) {
                final ServerEvent event = ServerEvent.create(type, channel, data);
                try {
                    session.sendMessage(soEventToMessage(event));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void send(String channel, Object data) {
                this.send(EVENT, channel, data);
            }

            @Override
            public Map<String, Object> data() {
                return sessionData;
            }
        };
        session.getAttributes().put(Wsock.class.getName(), wsock);
        wsockHandler.onConnect(wsock);
    }

    private WebSocketMessage<?> soEventToMessage(ServerEvent e) throws JsonProcessingException {
        return new TextMessage( wsockHandler.stringify(e) );
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Wsock wsock = (Wsock) session.getAttributes().get(Wsock.class.getName());
        wsockHandler.onDisconnect(wsock);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Wsock wsock = (Wsock) session.getAttributes().get(Wsock.class.getName());
        try {
            wsockHandler.handleTextMessage(wsock, message.getPayload());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
