package com.talk.chatter.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talk.chatter.Entities.Queue;
import com.talk.chatter.Repository.QueueRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class QueueService {

    @Autowired private QueueRepo queueRepo;

    public void queueMessage(
            String senderEmail,
            String receiverEmail,
            String messageText,
            LocalDateTime timestamp,
            ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> sessions) throws IOException {

        Map<String, String> messageObj = new HashMap<>();
        messageObj.put("senderId", senderEmail);
        messageObj.put("receiverId", receiverEmail);
        messageObj.put("messageText", messageText);
        messageObj.put("timestamp", String.valueOf(timestamp));

        ObjectMapper objectMapper = new ObjectMapper();
        String messageString = objectMapper.writeValueAsString(messageObj);

        List<WebSocketSession> receiverSessions = sessions.get(receiverEmail);
        boolean sent = false;

        // ✅ Updated: Support multi-sessions
        if (receiverSessions != null) {
            for (WebSocketSession receiverSession : receiverSessions) {
                if (receiverSession.isOpen()) {
                    receiverSession.sendMessage(new TextMessage(messageString));
                    sent = true;
                }
            }
        }

        // ✅ Only queue if message wasn't delivered to any session
        if (!sent) {
            Queue queueItem = new Queue();
            queueItem.setMessage(messageText);
            queueItem.setReceiverId(receiverEmail);
            queueItem.setSenderId(senderEmail);
            queueItem.setTimestamp(LocalDateTime.now());
            queueRepo.save(queueItem);
            System.out.println("Recipient not online. Queued message for: " + receiverEmail);
        } else {
            System.out.println("Sent message to at least one session of: " + receiverEmail);
        }
    }

    @Transactional
    public void deleteSentUnsentMessage(String username) {
        queueRepo.deleteByReceiverId(username);
    }
}
