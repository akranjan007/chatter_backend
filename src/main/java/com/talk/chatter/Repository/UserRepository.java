package com.talk.chatter.Repository;

import com.talk.chatter.Entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long>{
    Optional<Users> findByEmail(String email);
    boolean existsByEmail(String email);


}
