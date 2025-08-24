package com.talk.chatter.controller;

import com.talk.chatter.DTO.ChatRequestor;
import com.talk.chatter.DTO.MessageDTO;
import com.talk.chatter.Entities.Message;
import com.talk.chatter.Entities.Users;
import com.talk.chatter.Repository.ConnectionsRepo;
import com.talk.chatter.Repository.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/chat")
public class ChatController {
    @Autowired private MessageRepo messageRepo;
    @Autowired private ConnectionsRepo connectionsRepo;

    @PostMapping("/connections")
    public ResponseEntity<Map<String, Object>> getConnections(@RequestBody Map<String, String> request){
        String principalUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String userEmail = request.get("senderEmail");
        if(!principalUserEmail.equals(userEmail)){
            return  ResponseEntity.status(403).build();
        }
        //List<Users> connection = connectionsRepo.findConnectedUsers(userEmail);
        List<Object[]> connection = connectionsRepo.findConnectedUsersSortedByLastUpdated(userEmail);
        System.out.println(connection);
        List<Users> conn = connection.stream()
                .sorted((Obj1, Obj2) -> ((LocalDateTime) Obj2[1]).compareTo((LocalDateTime) Obj1[1]))
                .map(obj -> (Users) obj[0])
                .toList();
        Map<String, Object> response = new HashMap<>();
        response.put("data", conn);
        response.put("success", true);
        response.put("message", "Connection List Fetched.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/history/all")
    public ResponseEntity<Map<String, Object>> getAllChat(@RequestBody Map<String, String> request){
        Map<String, Object> response = new HashMap<>();
        try{
            String principalUser = SecurityContextHolder.getContext().getAuthentication().getName();
            String user = request.get("senderEmail");
            if(!principalUser.equals(user)){
                response.put("success", false);
                response.put("message", "Unauthorized access to chat.");
                return  ResponseEntity.status(403).body(response);
            }
            List<Message> messages = messageRepo.getAllChatHistory(user);
            Map<String, List<MessageDTO>> groupedMessages = messages.stream()
                    .map(mes -> new MessageDTO(mes.getSenderId(), mes.getReceiverId(), mes.getMessageText(), mes.getTimestamp()))
                    .collect(Collectors.groupingBy(m ->{
                        return m.getSenderId().equals(user) ? m.getReceiverId() : m.getSenderId();
                    }));
            response.put("success", true);
            response.put("data", groupedMessages);
            response.put("message", "All chats fetched.");
            return ResponseEntity.ok(response);
        }catch(Exception e){
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/history/single")
    public ResponseEntity<Map<String, Object>> getChat(@RequestBody ChatRequestor request){
        Map<String, Object> response = new HashMap<>();
        try{
            String principalUser = SecurityContextHolder.getContext().getAuthentication().getName();
            String user1 = request.getUser1();
            String user2 = request.getUser2();
            System.out.println(principalUser + " " + user1 + " " + user2);
            if(!principalUser.equals(user1) && !principalUser.equals(user2)){
                response.put("success", false);
                response.put("message", "Unauthorized access to chat.");
                return ResponseEntity.status(403).body(response);
            }
            List<Message>  messages = messageRepo.getChatHistory(user1, user2);
            List<MessageDTO> message = messages.stream()
                    .map(m -> new MessageDTO(m.getSenderId(), m.getReceiverId(), m.getMessageText(), m.getTimestamp()))
                    .collect(Collectors.toList());
            response.put("success", true);
            response.put("data", message);
            response.put("message", "Chat history fetched.");
            return ResponseEntity.ok(response);
        }catch(Exception e){
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
