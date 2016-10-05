package org.wsock.internal;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by joco on 02.10.16.
 */
public class WsockEvent {
    private long id;
    private WsockEventType type;
    private String channel;
    private Object data;

    private WsockEvent(long id, WsockEventType type, String channel, Object data) {
        this.id = id;
        this.type = type;
        this.channel = channel;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public WsockEventType getType() {
        return type;
    }

    public void setType(WsockEventType type) {
        this.type = type;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    private static final AtomicLong idgen = new AtomicLong(0);

    public static WsockEvent create(WsockEventType type, String channel, Object data) {
        return new WsockEvent(idgen.incrementAndGet(), type, channel, data);
    }
}
