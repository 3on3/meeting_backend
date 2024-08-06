
package  com.project.api.testChat;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.api.metting.dto.request.ChatMessageRequestDto;
import com.project.api.metting.dto.response.ChatMessageResponseDto;
import com.project.api.metting.entity.ChatMessage;
import com.project.api.metting.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MyWebSocketHandler extends TextWebSocketHandler {


    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions;

    public MyWebSocketHandler(Map<String, WebSocketSession> sessions) {
        this.objectMapper = new ObjectMapper();
        // localDateTime 을 제대로 받아오기 위한 코드
        this.objectMapper.registerModule(new JavaTimeModule());
        // withdraw 오류
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 추가된 설정
        this.sessions = sessions;
    }

    //최초 연결 시
    @OnOpen
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        final String sessionId = session.getId();

        if(!sessions.containsKey(sessionId)) {
            sessions.put(sessionId, session);
        }

    }

    //양방향 데이터 통신할 떄 해당 메서드가 call 된다.
    @OnMessage
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //do something
        final String sessionId = session.getId();

        ChatMessageRequestDto chatMessageRequestDto = objectMapper.readValue(message.getPayload(), ChatMessageRequestDto.class);

        sessions.values().forEach((s) -> {
                try {
                    String jsonMessage = objectMapper.writeValueAsString(chatMessageRequestDto);
                    s.sendMessage(new TextMessage(jsonMessage));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        });
    }

    //웹소켓 종료
    @OnClose
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        final String sessionId = session.getId();
        try {
            sessions.remove(sessionId); // 삭제
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //통신 에러 발생 시
    @OnError
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("session = " + session);
    }

    private void sendMessage(String sessionId, WebSocketMessage<?> message) {
        sessions.values().forEach(s -> {
            if(!s.getId().equals(sessionId) && s.isOpen()) {
                try {
                    s.sendMessage(message);
                } catch (IOException e) {}
            }
        });
    }



}