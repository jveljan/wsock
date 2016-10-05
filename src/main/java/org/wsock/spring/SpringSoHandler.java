package org.wsock.spring;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;
import org.wsock.pub.Wsock;
import org.wsock.internal.SoConnections;
import org.wsock.internal.SoEvent;
import org.wsock.internal.SoEventType;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.wsock.internal.SoEventType.EVENT;


/**
 * Created by joco on 01.10.16.
 */
public class SpringSoHandler extends TextWebSocketHandler {

    private SoConnections soConnections;

    public SpringSoHandler(SoConnections soConnections) {
        this.soConnections = soConnections;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(session.getUri()).build().getQueryParams();
        final String token = params.getFirst("token");
        if(token == null) {
            session.sendMessage(soEventToMessage(SoEvent.create(SoEventType.ERROR, "", "Invalid token")));
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        //TODO: duplicate token
        //TODO: ask services to accept this token

        final Wsock wsock = new Wsock() {
            private ConcurrentHashMap<String, Object> sessionData = new ConcurrentHashMap<>();

            @Override
            public String token() {
                return token;
            }

            @Override
            public void send(SoEventType type, String channel, Object data) {
                SoEvent event = SoEvent.create(type, channel, data);
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
        soConnections.onConnect(wsock);
    }

    private WebSocketMessage<?> soEventToMessage(SoEvent e) throws JsonProcessingException {
        return new TextMessage( soConnections.stringify(e) );
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Wsock wsock = (Wsock) session.getAttributes().get(Wsock.class.getName());
        soConnections.onDisconnect(wsock);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Wsock wsock = (Wsock) session.getAttributes().get(Wsock.class.getName());
        try {
            soConnections.handleTextMessage(wsock, message.getPayload());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
