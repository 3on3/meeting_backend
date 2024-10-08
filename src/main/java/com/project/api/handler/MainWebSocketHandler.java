package com.project.api.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.api.metting.dto.response.LoginResponseDto;
import com.project.api.metting.dto.response.MainWebSocketResponseDto;
import com.project.api.metting.repository.GroupRepository;
import com.project.api.metting.service.GroupService;
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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class MainWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MainWebSocketHandler.class);
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions;
    private final Map<String, LoginResponseDto> users = new ConcurrentHashMap<>();

    public MainWebSocketHandler(Map<String, WebSocketSession> sessions) {
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

        String sessionId = session.getId();

        MainWebSocketResponseDto mainWebSocketResponseDto = objectMapper.readValue(message.getPayload(), MainWebSocketResponseDto.class);


        if(mainWebSocketResponseDto.getType().equals("login")) {
            users.put(sessionId, mainWebSocketResponseDto.getLoginUser());
            System.out.println("users = " + users);
        }

        if(mainWebSocketResponseDto.getType().equals("matching")) {

            String email = mainWebSocketResponseDto.getEmail();

//            String email = "qwdk0529@naver.com";

            for (Map.Entry<String, LoginResponseDto> entry : users.entrySet()) {
                // Check if the email matches
                if (email.equals(entry.getValue().getEmail())) {
                    String hostSessionId = entry.getKey();

                    sessions.values().forEach((s) -> {
                        if (s.getId().equals(hostSessionId)) {
                            try {
                                String jsonMessage = objectMapper.writeValueAsString(mainWebSocketResponseDto.getResponseGroupId());
                                s.sendMessage(new TextMessage(jsonMessage));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }

        }

//        sessions.values().forEach((s) -> {
//            try {
//                String jsonMessage = objectMapper.writeValueAsString(null);
//                s.sendMessage(new TextMessage(jsonMessage));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        });
    }

    //웹소켓 종료
    @OnClose
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        final String sessionId = session.getId();
        try {
            users.remove(sessionId);
            sessions.remove(sessionId); // 삭제

            System.out.println("users = " + users);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    //통신 에러 발생 시
    @OnError
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

        System.out.println("session = " + session);


    }

}
