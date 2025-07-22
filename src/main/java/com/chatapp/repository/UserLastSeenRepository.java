package com.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatapp.model.UserLastSeen;



public interface UserLastSeenRepository extends JpaRepository<UserLastSeen, Long> {
    // Add custom queries if needed
}