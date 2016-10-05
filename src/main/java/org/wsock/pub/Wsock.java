package org.wsock.pub;


import org.wsock.internal.model.WsockEventType;

import java.util.Map;

/**
 * Socket Session
 *
 * Created by joco on 02.10.16.
 */
public interface Wsock {
    /**
     * get client token
     */
    String token();

    /**
     * Sends an event
     */
    void send(WsockEventType type, String channel, Object data);

    /**
     * sends type EVENT
     */
    void send(String channel, Object data);

    /**
     * Socket session data
     */
    Map<String, Object> data();
}
