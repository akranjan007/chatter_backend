package com.talk.chatter.Services;

import com.talk.chatter.Entities.Connections;
import com.talk.chatter.Entities.Users;
import com.talk.chatter.Repository.ConnectionsRepo;
import com.talk.chatter.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConnectionService {
    @Autowired UserRepository userRepo;
    @Autowired ConnectionsRepo connectionsRepo;

    public void connectionCheck(String senderEmail, String receiverEmail){
        Optional<Users> user1Opt = userRepo.findByEmail(senderEmail);
        Optional<Users> user2Opt = userRepo.findByEmail(receiverEmail);

        if(user2Opt.isEmpty() || user1Opt.isEmpty()){
            System.out.println("Either of sender or receiver doesn't exists in DB.");
            return;
        }
        Users user1 = user1Opt.get().getUserId()<user2Opt.get().getUserId() ? user1Opt.get() : user2Opt.get();
        Users user2 = user1Opt.get().getUserId()<user2Opt.get().getUserId() ? user2Opt.get() : user1Opt.get();

        if(!connectionsRepo.exitsBetweenUsers(user1.getUserId(), user2.getUserId())){
            Connections conn = new Connections();
            conn.setUser1(user1);
            conn.setUser2(user2);
            conn.setLastUpdated(LocalDateTime.now());
            connectionsRepo.save(conn);
            System.out.println("Connections started between "+user1.getEmail()+" and "+user2.getEmail());
        }
    }

    public void updateLastUpdated(String senderEmail, String receiverEmail){
        Long userAId = userRepo.findByEmail(senderEmail).get().getUserId();
        Long userBId = userRepo.findByEmail(receiverEmail).get().getUserId();
        connectionsRepo.updateLastUpdated(userAId, userBId);
    }
}
