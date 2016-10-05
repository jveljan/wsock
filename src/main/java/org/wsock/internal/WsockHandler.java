package org.wsock.internal;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jodah.typetools.TypeResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.wsock.internal.model.ClientEvent;
import org.wsock.internal.model.RespErrorMessage;
import org.wsock.internal.model.ServerEvent;
import org.wsock.internal.model.WsockEventType;
import org.wsock.pub.Wsock;

import java.io.IOException;
import java.util.*;
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

    public Object onMessage(Wsock session, ServerEvent message) {
        log.debug("Socket message on channel: {} ", message.getChannel());

        final Function fn = registry.getHandler(message.getChannel());

        final BiConsumer<Wsock, ServerEvent> onMessageCb = registry.getOnMessageCallback();
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
            final BiConsumer<Wsock, ServerEvent> unhandledMessageCallback = registry.getOnUnhandledMessageCallback();
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




    public void handleTextMessage(Wsock wsock, String payload) throws IOException {
        if(payload == null || payload.startsWith("$")) {
            //TODO: handle special messages, eg. $heartbeat
            return;
        }
        ClientEvent clientEvent = parse(payload, ClientEvent.class);
        //TODO: maybe we need some kind of intermediate data structure here
        ServerEvent e = ServerEvent.create(WsockEventType.REQ, clientEvent.getPath(), clientEvent.getData());
        Object resp = this.onMessage(wsock, e);
        if(resp != null) {
            final String respChannel = getRespChannel(clientEvent);
            if(resp instanceof Exception) {
                wsock.send(WsockEventType.ERROR, respChannel, new RespErrorMessage((Exception) resp));
            } else {
                wsock.send(WsockEventType.RESP, respChannel, resp);
            }
        }
    }

    private String getRespChannel(ClientEvent clientEvent) {
        StringBuilder sb = new StringBuilder();
        sb.append(clientEvent.getPath());
        if(clientEvent.getMessageId() != null) {
            sb.append("#");
            sb.append(clientEvent.getMessageId());
        }
        return sb.toString();
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
