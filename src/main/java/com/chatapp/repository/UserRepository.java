package com.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.chatapp.model.User;
import org.springframework.stereotype.Repository;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // You can add custom queries if necessary
    User findByUsername(String username);
    User findByEmail(String email);
}
