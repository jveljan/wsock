package org.wsock;

import org.springframework.stereotype.Service;
import org.wsock.pub.WebSocket;
import org.wsock.pub.WsockService;
import org.wsock.pub.WsockInit;

/**
 * Created by joco on 05.10.16.
 */
@WebSocket("/wsock/endpoint")
@Service
public class WsockExampleEndpoint implements WsockInit {
    @Override
    public void init(WsockService sockets) {

        sockets.onConnect(wsock -> "connected");

        sockets.on("/ping", (Void arg) -> {
            return "pong";
        });

        sockets.on("/chat/message", (String msg) -> {
            sockets.broadcast("connected", "/chart/on/message", msg);
        });

    }
}
