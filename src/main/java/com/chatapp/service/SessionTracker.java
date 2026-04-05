package com.chatapp.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp.dto.UserStatusChangedEvent;
import com.chatapp.dto.UserStatusDto;
import com.chatapp.model.UserLastSeen;
import com.chatapp.repository.UserLastSeenRepository;
import com.chatapp.service.RedisService;


@Component
public class SessionTracker {

    private final UserLastSeenRepository repository;
    private final RedisService redisService;
    private final SimpMessagingTemplate messagingTemplate;

    public SessionTracker(UserLastSeenRepository repository, RedisService redisService, SimpMessagingTemplate messagingTemplate) {
        this.repository = repository;
        this.redisService = redisService;
        this.messagingTemplate = messagingTemplate;
    }

    public void updateHeartbeat(long userId) {
        UserLastSeen userLastSeen = repository.findById(userId)
            .orElse(new UserLastSeen(userId, true, Instant.now()));

        userLastSeen.setLastSeen(Instant.now());
        userLastSeen.setOnline(true);

        repository.save(userLastSeen);
    }

    public void setOffline(long userId) {
        repository.findById(userId).ifPresent(userLastSeen -> {
            userLastSeen.setOnline(false);
            userLastSeen.setLastSeen(Instant.now());
            repository.save(userLastSeen);
        });
    }

    @Scheduled(fixedRate = 600_000) // every 10 minutes
    @Transactional
    public void cleanupStaleUsers() {
        Instant threshold = Instant.now().minus(Duration.ofMinutes(1)); // example threshold
        List<UserLastSeen> staleUsers = repository.findAll().stream()
            .filter(u -> u.isOnline() && u.getLastSeen().isBefore(threshold))
            .toList();

        staleUsers.forEach(user -> {
            user.setOnline(false);
            repository.save(user);

            Long userId = user.getUserId(); 
            redisService.updateUserLastSeen(userId.toString());
            redisService.removeAllSessionsForUser(userId.toString());
            System.out.println("setting stale user offline for user: " + user);

        });
    }

   public void emitUserStatus(String userId, boolean isOnline) {
    Set<String> receivers = redisService.getUsersWhoChattedWith(userId);
    // Filter out groups
    receivers = receivers.stream()
        .filter(id -> !id.startsWith("group_"))
        .collect(Collectors.toSet());

    for (String receiverId : receivers) {
        String normalizedReceiverId = receiverId.startsWith("user_") 
            ? receiverId.substring(5) : receiverId;
        messagingTemplate.convertAndSend("/topic/status/" + normalizedReceiverId, new UserStatusDto(userId, isOnline));
        System.out.println("Emitting status to " + normalizedReceiverId);
    }
}
}
