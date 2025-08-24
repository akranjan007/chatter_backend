package com.talk.chatter.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class StatusService {
    private boolean statusOnline ;

    public boolean isStatusOnline(String checkUser, ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> sessions) {
        return sessions.containsKey(checkUser);
    }

    public void setStatusOnline(boolean statusOnline) {
        this.statusOnline = statusOnline;
    }
}

