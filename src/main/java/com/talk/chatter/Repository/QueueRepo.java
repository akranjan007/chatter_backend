package com.talk.chatter.Repository;

import com.talk.chatter.Entities.Queue;
import com.talk.chatter.Entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QueueRepo extends JpaRepository<Queue, Long> {
    boolean existsByReceiverId(String receiverId);
    List<Queue> findByReceiverId(String receiverId);
    void deleteByReceiverId(String receiverId);
}
