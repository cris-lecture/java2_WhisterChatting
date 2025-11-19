package edu.ewha.java2;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatHandler extends TextWebSocketHandler {
	private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {        

//		System.out.println("원본 URI: " + session.getUri());
//		System.out.println("Query: " + session.getUri().getQuery());
		String username = getUsername(session); // 안전하게 로컬 변수로 받기
		System.out.println("접속한 사용자: " + username);
		userSessions.put(username, session);

	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		String username = getUsername(session); // 세션에서 다시 추출
		userSessions.remove(username);
		System.out.println("연결 종료: " + username);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
		String[] parts = message.getPayload().split("\\|", 3);  // user|to|message

		if (parts.length < 3) {
			System.out.println("정보 부족! return.");
			return;
		}

		//String from = parts[0];
		String from = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
		String to = parts[1];
		String msg = parts[2];
		
		
		if (to.equalsIgnoreCase("all")) {
			for (WebSocketSession s : userSessions.values()) {
				System.out.println(userSessions.size());
				if (s.isOpen()) {
					s.sendMessage(new TextMessage(from + ": " + msg));
				}
			}
		} else {
			WebSocketSession target = userSessions.get(to);
			if (target != null && target.isOpen()) {
				target.sendMessage(new TextMessage("[귓속말] " + from + ": " + msg));
			}
		}
	}

	private String getUsername(WebSocketSession session) {
		String query = session.getUri().getQuery(); // ?user=홍길동
		System.out.println(query);
		if (query != null && query.startsWith("user=")) {

			String decodedUsername = URLDecoder.decode(query.substring(5), StandardCharsets.UTF_8);
			System.out.println("변경된 이름 : " + decodedUsername);  
			return  decodedUsername;
		}
		return "unknown";
	}
}
