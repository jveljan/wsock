package org.wsock.pub;


import org.wsock.internal.SoEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by joco on 02.10.16.
 */
public interface WsockService {
    /**
     * sessions are grouped in buckets by key returned in connectCallback
     * if you don't want to accept the currentConn, throw exception from this function
     */
    void onConnect(Function<Wsock, String> connectCallback);

    /**
     * just clean up disconnect callback
     */
    void onDisconnect(Consumer<Wsock> disconnectCallback);

    /**
     * onMessage event
     */
    void onMessage(BiConsumer<Wsock, SoEvent> onMessageCallback);

    /**
     * onMessage but only if there is no handler for that event
     */
    void onUnhandledMessage(BiConsumer<Wsock, SoEvent> onMessageCallback);

    /**
     * Extracted event data on path listener
     */
    <I> void on(String path, Function<I, ?> fn);

    <I> void on(String path, Consumer<I> fn);

    /**
     * Broadcast message to sessions in a bucket
     */
    void broadcast(String bucket, String path, Object data);

    /**
     * Should be called onlu inside handler methods
     *
     * @return Current Session
     */
    Wsock current();
}
