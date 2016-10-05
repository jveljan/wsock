package org.wsock.pub;

/**
 * Created by joco on 03.10.16.
 *
 * Implement this annotated with WebSocket to create end point
 * eg.
 * {@literal @}WebSocket('/my/socket')
 * public MySocket implements WsockInit {
 *     init(WsockService wsock) {
 *         wsock.onConnect(session -> "connected");
 *         wsock.on("/chat/message", (String msg) -> {
 *             wsock.broadcast("connected", msg);
 *         });
 *     }
 * }
 *
 */
public interface WsockInit {
    void init(WsockService wsockService);
}
