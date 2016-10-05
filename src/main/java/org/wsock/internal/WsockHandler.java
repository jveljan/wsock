package org.wsock.internal;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jodah.typetools.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.wsock.pub.Wsock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by joco on 02.10.16.
 */
public class WsockHandler {
    private static final String NULL_BUCKET_KEY = "@NULL";
    private static final Logger log = LoggerFactory.getLogger(WsockHandler.class);
    private ThreadLocal<Wsock> soSessionThreadLocal = new ThreadLocal<>();
    private Map<String, List<Wsock>> sessionBuckets = new ConcurrentHashMap<>();

    private WsockRegistry registry = new WsockRegistry();
    private ObjectMapper objectMapper;

    @Autowired
    public WsockHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void onConnect(Wsock session) {
        String bucket = null;
        if(registry.getConnectCallback() != null) {
            bucket = registry.getConnectCallback().apply(session);
        }
        if(bucket == null) {
            bucket = NULL_BUCKET_KEY;
        }

        List<Wsock> sessions = sessionBuckets.computeIfAbsent(bucket, k -> {
           return new LinkedList<Wsock>();
        });
        synchronized (sessionBuckets) {
            sessions.add(session);
        }
    }

    public Object onMessage(Wsock session, WsockEvent message) {
        log.debug("Socket message on channel: {} ", message.getChannel());

        final Function fn = registry.getHandler(message.getChannel());

        final BiConsumer<Wsock, WsockEvent> onMessageCb = registry.getOnMessageCallback();
        if(onMessageCb != null) {
            try {
                onMessageCb.accept(session, message);
            } catch (Exception e){
                log.error("Error in onMessage handler", e);
            }
        }

        if(fn != null) {
            soSessionThreadLocal.set(session);
            try {
                //convert data to correct type
                final Class<?>[] args = TypeResolver.resolveRawArguments(Function.class, fn.getClass());

                // TODO: check if we can do this better
                // message.getData() is Object, but at the time of initial parse there is no exact type
                // to it is converted into Map
                final Object data = parse(stringify(message.getData()), args[0]);

                // return data from handler
                return fn.apply(data);

            } catch (Exception e) {
                log.error("Oops, ", e);
                return e;
            } finally {
                soSessionThreadLocal.remove();
            }
        } else {
            final BiConsumer<Wsock, WsockEvent> unhandledMessageCallback = registry.getOnUnhandledMessageCallback();
            if(unhandledMessageCallback != null) {
                try {
                    unhandledMessageCallback.accept(session, message);
                } catch (Exception e) {
                    log.error("Error in unhandled message handler", e);
                }
            }
        }

        return null;
    }

    public Wsock getCurrentSession() {
        return soSessionThreadLocal.get();
    }

    private String findSessionBucket(Wsock session) {
        for(String bucket : sessionBuckets.keySet()) {
            List<Wsock> list = sessionBuckets.get(bucket);
            if(list.contains(session)) {
                return bucket;
            }
        }
        return null;
    }

    public void onDisconnect(Wsock session) {
        final String bucket = findSessionBucket(session);
        if(bucket == null) {
            throw new IllegalStateException("No bucket found for currentConn " + session);
        }
        synchronized (sessionBuckets) {
            List<Wsock> sessions = sessionBuckets.get(bucket);
            if(sessions.size() == 1) {
                sessionBuckets.remove(bucket);
            } else {
                sessions.remove(session);
            }
        }
        if(registry.getDisconnectCallback() != null) {
            registry.getDisconnectCallback().accept(session);
        }
    }

    public void broadcast(String bucket, String path, Object data) {
        List<Wsock> sessions = sessionBuckets.getOrDefault(bucket, new ArrayList<>());
        sessions.forEach(session -> {
            try {
                session.send(path, data);
            } catch (Exception e) {
                log.error("Error sending data to session {} ", session, e);
            }
        });
    }



    static class Req {
        public String path;
        public Object data;

        // TODO: this needs better name
        // it its response identifier for the listener
        public String clientId;
    }

    public void handleTextMessage(Wsock wsock, String payload) throws IOException {
        if(payload == null || payload.startsWith("$")) {
            //TODO: handle special messages, eg. $heartbeat
            return;
        }
        Req req = parse(payload, Req.class);
        WsockEvent e = WsockEvent.create(WsockEventType.REQ, req.path, req.data);
        Object resp = this.onMessage(wsock, e);
        if(resp != null) {
            //TODO: what if clientId is null ? ...
            // just send back req.path if no clientId provided ...

            String respChannel = req.path + (req.clientId != null ? "#" + req.clientId : "");
            if(resp instanceof Exception) {
                wsock.send(WsockEventType.ERROR, respChannel, new RespErrorMessage((Exception) resp));
            } else {
                wsock.send(WsockEventType.RESP, respChannel, resp);
            }
        }
    }


    public String stringify(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public <T> T parse(String str, Class<T> type) throws IOException {
        return objectMapper.readValue(str, type);
    }

    public WsockRegistry getRegistry() {
        return registry;
    }
}
