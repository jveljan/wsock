package org.wsock.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wsock.internal.model.ServerEvent;
import org.wsock.pub.Wsock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by joco on 02.10.16.
 */
public class WsockRegistry {
    private static final Logger log = LoggerFactory.getLogger(WsockRegistry.class);

    private ConcurrentHashMap<String, Function> handlersMap = new ConcurrentHashMap<>();
    private Function<Wsock, String> connectCallback;
    private Consumer<Wsock> disconnectCallback;
    private BiConsumer<Wsock, ServerEvent> onMessageCallback;
    private BiConsumer<Wsock, ServerEvent> onUnhandledMessageCallback;


    public <I> void addHandler(String path, Function<I, ?> handler) {
        log.debug("Registering socket handler on path: {}", path);
        handlersMap.put(path, handler);
    }

    public Function getHandler(String path) {
        return handlersMap.get(path);
    }


    public void setConnectCallback(Function<Wsock, String> connectCallback) {
        this.connectCallback = connectCallback;
    }

    public void setDisconnectCallback(Consumer<Wsock> disconnectCallback) {
        this.disconnectCallback = disconnectCallback;
    }

    public void setOnMessageCallback(BiConsumer<Wsock, ServerEvent> onMessageCallback) {
        this.onMessageCallback = onMessageCallback;
    }

    public Function<Wsock, String> getConnectCallback() {
        return connectCallback;
    }

    public Consumer<Wsock> getDisconnectCallback() {
        return disconnectCallback;
    }

    public BiConsumer<Wsock, ServerEvent> getOnMessageCallback() {
        return onMessageCallback;
    }

    public void setOnUnhandledMessageCallback(BiConsumer<Wsock, ServerEvent> onUnhandledMessageCallback) {
        this.onUnhandledMessageCallback = onUnhandledMessageCallback;
    }
    public BiConsumer<Wsock, ServerEvent> getOnUnhandledMessageCallback() {
        return onUnhandledMessageCallback;
    }
}
