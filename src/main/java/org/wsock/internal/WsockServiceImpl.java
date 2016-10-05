package org.wsock.internal;


import org.wsock.pub.WsockService;
import org.wsock.pub.Wsock;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static javafx.scene.input.KeyCode.T;

/**
 * Created by joco on 02.10.16.
 */
public class WsockServiceImpl implements WsockService {
    private SoConnections soConnections;

    public WsockServiceImpl(SoConnections soConnections) {
        this.soConnections = soConnections;
    }

    @Override
    public void onConnect(Function<Wsock, String> connectCallback) {
        soConnections.getRegistry().setConnectCallback(connectCallback);
    }

    @Override
    public void onDisconnect(Consumer<Wsock> disconnectCallback) {
        soConnections.getRegistry().setDisconnectCallback(disconnectCallback);
    }

    @Override
    public void onMessage(BiConsumer<Wsock, SoEvent> onMessageCallback) {
        soConnections.getRegistry().setOnMessageCallback(onMessageCallback);
    }

    @Override
    public void onUnhandledMessage(BiConsumer<Wsock, SoEvent> onMessageCallback) {
        soConnections.getRegistry().setOnUnhandledMessageCallback(onMessageCallback);
    }

    @Override
    public <I> void on(String path, Function<I, ?> fn) {
        soConnections.getRegistry().addHandler(path, fn);
    }

    @Override
    public <I> void on(String path, Consumer<I> fn) {
        soConnections.getRegistry().addHandler(path, (I req) -> {
            fn.accept(req);
            return null;
        });
    }

    @Override
    public void broadcast(String bucket, String path, Object data) {
        soConnections.broadcast(bucket, path, data);
    }

    @Override
    public Wsock current() {
        return soConnections.getCurrentSession();
    }
}
