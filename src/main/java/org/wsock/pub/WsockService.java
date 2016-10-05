package org.wsock.pub;


import org.wsock.internal.WsockEvent;

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
    void onMessage(BiConsumer<Wsock, WsockEvent> onMessageCallback);

    /**
     * onMessage but only if there is no handler for that event
     */
    void onUnhandledMessage(BiConsumer<Wsock, WsockEvent> onMessageCallback);

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

    /**
     * Register this to accept/reject tokens from establishing socket connection
     * (if not set, all tokens will be accepted)
     *
     * @param tokenAcceptor
     */
    void tokenAcceptor(Function<String, Boolean> tokenAcceptor);

    /**
     * Modify default configuration
     *
     * @param configurator
     */
    void configuration(Consumer<WsockConfig> configurator);
}
