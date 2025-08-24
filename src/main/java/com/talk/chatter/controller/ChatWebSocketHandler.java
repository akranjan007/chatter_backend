package com.talk.chatter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talk.chatter.DTO.ChatMessageDTO;
import com.talk.chatter.Entities.Queue;
import com.talk.chatter.Repository.*;
import com.talk.chatter.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    @Autowired private QueueRepo queueRepo;
    @Autowired private MessageRepo messageRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private QueueService queueService;
    @Autowired private ConnectionsRepo connectionsRepo;
    @Autowired private ConnectionService connectionService;
    @Autowired private MessageService messageService;
    @Autowired private StatusService statusService;

    //Updated: Support multiple sessions per user
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    public ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> getSessions(){
        return sessions;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = (String) session.getAttributes().get("username");

        if (username != null) {
            //Added: Support multiple sessions
            sessions.computeIfAbsent(username, k -> new CopyOnWriteArrayList<>()).add(session);
            System.out.println("Connection Established : " + username);
            printAllSessions();
            List<Queue> unsent = queueRepo.findByReceiverId(username);
            if (!unsent.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();

                for (Queue q : unsent) {
                    Map<String, String> messageObj = new HashMap<>();
                    messageObj.put("senderId", q.getSenderId());
                    messageObj.put("receiverId", q.getReceiverId());
                    messageObj.put("messageText", q.getMessage());
                    messageObj.put("timestamp", String.valueOf(q.getTimestamp()));

                    String messageString = objectMapper.writeValueAsString(messageObj);

                    List<WebSocketSession> userSessions = sessions.get(username);
                    if (userSessions != null) {
                        for (WebSocketSession ws : userSessions) {
                            if (ws.isOpen()) {
                                ws.sendMessage(new TextMessage(messageString));
                            }
                        }
                    }
                }

                queueService.deleteSentUnsentMessage(username); // Keep if messages are per user
            } else {
                System.out.println("No new Message for " + username);
            }
        } else {
            System.out.println("Username Missing. Closing connection.");
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        System.out.println("Message : " + message.getPayload());

        String senderEmail = (String) session.getAttributes().get("username");
        ObjectMapper mapper = new ObjectMapper();
        ChatMessageDTO chatMessageDTO = mapper.readValue(message.getPayload().toString(), ChatMessageDTO.class);

        String receiverEmail = chatMessageDTO.getReceiverId();
        String messageText = chatMessageDTO.getMessageText();

        if (receiverEmail == null || messageText == null) {
            System.out.println("Invalid message format, missing receiverId or messageText");
            return;
        }
        if (!userRepo.existsByEmail(receiverEmail)) {
            System.out.println("Receiver doesn't exist: " + receiverEmail);
            return;
        }

        LocalDateTime timestamp = LocalDateTime.now();
        connectionService.connectionCheck(senderEmail, receiverEmail);
        messageService.saveMessage(senderEmail, receiverEmail, messageText, timestamp);
        connectionService.updateLastUpdated(senderEmail, receiverEmail);

        Map<String, String> messageObj = new HashMap<>();
        messageObj.put("senderId", senderEmail);
        messageObj.put("receiverId", receiverEmail);
        messageObj.put("messageText", messageText);
        messageObj.put("timestamp", timestamp.toString());

        ObjectMapper objectMapper = new ObjectMapper();
        String messageString = objectMapper.writeValueAsString(messageObj);

        List<WebSocketSession> receiverSessions = sessions.get(receiverEmail);
        boolean sent = false;

        if (receiverSessions != null) {
            for (WebSocketSession receiverSession : receiverSessions) {
                if (receiverSession.isOpen()) {
                    receiverSession.sendMessage(new TextMessage(messageString));
                    sent = true;
                }
            }
        }

        if (!sent) {
            //Updated: Pass sessions correctly and handle multi-session
            queueService.queueMessage(senderEmail, receiverEmail, messageText, timestamp, sessions);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.out.println("Transport Error: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String username = (String) session.getAttributes().get("username");

        if (username != null) {
            //Updated: Remove only the disconnected session
            CopyOnWriteArrayList<WebSocketSession> userSessions = sessions.get(username);
            if (userSessions != null) {
                userSessions.remove(session);
                if (userSessions.isEmpty()) {
                    sessions.remove(username);
                }
            }
            System.out.println("Connection Closed: " + username);
        } else {
            System.out.println("Connection closed: Unknown User.");
            session.close(CloseStatus.NOT_ACCEPTABLE);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    public void printAllSessions(){
        for(Map.Entry<String, CopyOnWriteArrayList<WebSocketSession>> entry: sessions.entrySet()){
            String userName = entry.getKey();
            CopyOnWriteArrayList<WebSocketSession> sessionList = entry.getValue();
            System.out.println("UserName: "+ userName);
            for(WebSocketSession session:sessionList){
                System.out.println("Session Id: "+ session.getId());
                System.out.println("Is Open: "+ session.isOpen());
                System.out.println("Remote Address: "+ session.getRemoteAddress());
                //System.out.println("Session Id: "+ session.getPrincipal());
            }
        }
    }
}
