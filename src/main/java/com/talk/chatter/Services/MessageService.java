package com.talk.chatter.Services;

import com.talk.chatter.Entities.Message;
import com.talk.chatter.Repository.MessageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MessageService {
    @Autowired private MessageRepo messageRepo;

    public void saveMessage(String senderEmail, String receiverEmail, String messageText, LocalDateTime timestamp){
        Message messageItem = new Message();
        messageItem.setMessageText(messageText);
        messageItem.setReceiverId(receiverEmail);
        messageItem.setSenderId(senderEmail);
        messageItem.setTimestamp(timestamp);
        messageRepo.save(messageItem);
    }
}
