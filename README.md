# Wsock

The simplest websocket procotol I could come up for easy and pubsub-ish / req-resp way communication between browser (HTML5 WebSocket) and Java (spring boot)

# Protocol

Message exchange is always in json (*)

```javascript
{
  path: "/"
  data: {}
}
```

But, lets first talk about final handling not worrying about the protocol for now.

# Server
Assuming running spring-boot, include this and implement org.wsock.pub.WsockInit

```java
@WebSocket("/wsock/endpoint")
@Service
public class WsockExampleEndpoint implements WsockInit {
    @Override
    public void init(WsockService sockets) {

        sockets.onConnect(wsock -> "connected");

        sockets.on("/ping", (Void arg) -> {
            return "pong";
        });

        sockets.on("/chat/message", (String msg) -> {
            sockets.broadcast("connected", "/chart/on/message", msg);
        });

    }
}
```

Autoconfiguration will pick all WsocInit and register handlers (in this case only one at: /wsock/endpoint)

Messages are automaticaly converted and routed to provided handlers...


# Client
```javascript

ws.send('/ping', function(resp) {
  console.log(resp); // pong
});

ws.send('/chat/message', 'hello');

ws.on('/char/on/message', function(msg) {
});

```

