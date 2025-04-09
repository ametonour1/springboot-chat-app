package com.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.chatapp.model.User;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // You can add custom queries if necessary
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
    //User findByEmail(String email);
}
