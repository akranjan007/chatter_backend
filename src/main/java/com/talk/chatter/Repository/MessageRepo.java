package com.talk.chatter.Repository;

import com.talk.chatter.Entities.Message;
import com.talk.chatter.Entities.Queue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepo extends JpaRepository<Message, Long> {
    @Query(
            "SELECT m FROM Message m WHERE " +
                    "(m.senderId = :user1 AND m.receiverId = :user2) OR " +
                    "(m.senderId = :user2 AND m.receiverId = :user1) " +
                    "ORDER BY m.timestamp ASC"
    )
    List<Message> getChatHistory(@Param("user1") String user1, @Param("user2") String user2);

    @Query(
        "SELECT m FROM Message m WHERE "+
                "m.senderId = :user OR m.receiverId = :user " +
                "ORDER BY m.timestamp ASC"
    )
    List<Message> getAllChatHistory(@Param("user") String user);
}
