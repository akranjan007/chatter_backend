package com.talk.chatter.controller;

import com.talk.chatter.Entities.Users;
import com.talk.chatter.Repository.UserRepository;
import com.talk.chatter.Services.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@CrossOrigin
@RequestMapping("/search")
public class SearchController {
    @Autowired private UserRepository userRepository;
    @Autowired private StatusService statusService;
    @Autowired private ChatWebSocketHandler chatWebSocketHandler;

    @PostMapping("/user")
    public ResponseEntity<Map<String, Object>> searchUser(@RequestBody Map<String, String> request){
        Map<String, Object> response = new HashMap<>();
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        String searchUser = request.get("searchUser");
        if(principal==null || searchUser==null || searchUser.trim().isEmpty()){
            response.put("success", false);
            response.put("message", "User not Logged in or SearchUser not received.");
            return ResponseEntity.status(400).body(response);
        }
        try{
            if(userRepository.existsByEmail(searchUser)){
                Optional<Users> user = userRepository.findByEmail(searchUser);
                response.put("success", true);
                response.put("data", user);
                response.put("message", "User Found.");
                return ResponseEntity.ok(response);
            }
            else{
                response.put("success", false);
                response.put("message", "User does not exists.");
                return ResponseEntity.status(404).body(response);
            }
        } catch(Exception e){
            e.printStackTrace();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> checkStatus(@RequestBody Map<String, String> req){
        Map<String, Object> response = new HashMap<>();
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        String checkUser = req.get("checkUser");
        if(principal==null || checkUser==null || checkUser.trim().isEmpty()){
            response.put("success", false);
            response.put("message", "User not logged in or, CheckUser not received.");
            return ResponseEntity.status(400).body(response);
        }
        try{
            String userStatus = statusService.isStatusOnline(checkUser, chatWebSocketHandler.getSessions()) ? "Online" : "Offline";
            response.put("success", true);
            response.put("status", userStatus);
            return ResponseEntity.ok(response);
        } catch(Exception e){
            response.put("success", false);
            response.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/concurrent")
    public ResponseEntity<Map<String, Object>> checkConcurrent(){
        Map<String, Object> response = new HashMap<>();
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        //String checkUser = req.get("checkUser");
        /*if(principal==null || checkUser==null || checkUser.trim().isEmpty()){
            response.put("success", false);
            response.put("message", "User not logged in or, CheckUser not received.");
            return ResponseEntity.status(400).body(response);
        }*/
        if(principal==null){
            response.put("success", false);
            response.put("message", "User not logged in.");
            return ResponseEntity.status(400).body(response);
        }
        try{
            ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> userStatus = chatWebSocketHandler.getSessions();
            List<String> keys = new ArrayList<>(userStatus.keySet());
            //System.out.println("concurrent print");
            //System.out.println(keys);
            response.put("success", true);
            response.put("data", keys);
            return ResponseEntity.ok(response);
        } catch(Exception e){
            response.put("success", false);
            response.put("message", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }
}
