package org.wsock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WsockApplicationTests {


	@LocalServerPort
	int port;

	@Autowired
	private ObjectMapper objectMapper;

	private Map<String, Object> messages;

	@Before
	public void init() throws IOException {
		messages = objectMapper.readValue(getClass().getResourceAsStream("/objects.json"), HashMap.class);
	}

	private TextMessage getMessage(String key) throws JsonProcessingException {
		return new TextMessage(objectMapper.writeValueAsString(messages.get(key)));
	}

	@Test
	public void contextLoads() throws IOException, InterruptedException {

		StandardWebSocketClient client = new StandardWebSocketClient();
		client.doHandshake(new AbstractWebSocketHandler() {
			@Override
			public void afterConnectionEstablished(WebSocketSession session) throws Exception {
				session.sendMessage(getMessage("pingMessage"));
				session.sendMessage(getMessage("chatMessage"));
			}

			@Override
			protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
				System.out.println("Got Message: " + message);
				System.out.println(" --Full Payload: " + message.getPayload());
			}

			@Override
			public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
				super.afterConnectionClosed(session, status);
			}
		}, "ws://localhost:" + port + "/wsock/endpoint?token=1234");


		Thread.sleep(2000);
	}

}
