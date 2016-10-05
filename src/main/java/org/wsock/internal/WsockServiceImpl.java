package org.wsock.internal;


import org.wsock.pub.WsockConfig;
import org.wsock.pub.WsockService;
import org.wsock.pub.Wsock;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by joco on 02.10.16.
 */
public class WsockServiceImpl implements WsockService {
    private WsockHandler wsockHandler;
    private Function<String, Boolean> tokenAcceptor;
    private Consumer<WsockConfig> configurator;

    public WsockServiceImpl(WsockHandler wsockHandler) {
        this.wsockHandler = wsockHandler;
    }

    @Override
    public void onConnect(Function<Wsock, String> connectCallback) {
        wsockHandler.getRegistry().setConnectCallback(connectCallback);
    }

    @Override
    public void onDisconnect(Consumer<Wsock> disconnectCallback) {
        wsockHandler.getRegistry().setDisconnectCallback(disconnectCallback);
    }

    @Override
    public void onMessage(BiConsumer<Wsock, WsockEvent> onMessageCallback) {
        wsockHandler.getRegistry().setOnMessageCallback(onMessageCallback);
    }

    @Override
    public void onUnhandledMessage(BiConsumer<Wsock, WsockEvent> onMessageCallback) {
        wsockHandler.getRegistry().setOnUnhandledMessageCallback(onMessageCallback);
    }

    @Override
    public <I> void on(String path, Function<I, ?> fn) {
        wsockHandler.getRegistry().addHandler(path, fn);
    }

    @Override
    public <I> void on(String path, Consumer<I> fn) {
        wsockHandler.getRegistry().addHandler(path, (I req) -> {
            fn.accept(req);
            return null;
        });
    }

    @Override
    public void broadcast(String bucket, String path, Object data) {
        wsockHandler.broadcast(bucket, path, data);
    }

    @Override
    public Wsock current() {
        return wsockHandler.getCurrentSession();
    }

    @Override
    public void tokenAcceptor(Function<String, Boolean> tokenAcceptor) {
        this.tokenAcceptor = tokenAcceptor;
    }

    public Function<String, Boolean> getTokenAcceptor() {
        return tokenAcceptor;
    }

    @Override
    public void configuration(Consumer<WsockConfig> configurator) {
        this.configurator = configurator;
    }

    public Consumer<WsockConfig> getConfigurator() {
        return configurator;
    }
}
