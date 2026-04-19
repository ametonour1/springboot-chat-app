# springboot-chat-app
SecureStream: E2EE Real-Time Messaging Platform
SecureStream is a high-performance, security-focused messaging engine built to handle real-time communication with a "Privacy-First" philosophy. It combines the reliability of Spring Boot with the scalability of Apache Kafka and the security of End-to-End Encryption (E2EE).

Key Features
End-to-End Encryption (E2EE): Zero-knowledge architecture where message content is encrypted/decrypted only on client devices.

Real-Time Communication: Full-duplex messaging using WebSockets (STOMP) for instant delivery and signaling.

Event-Driven Architecture: High-throughput message processing and service decoupling powered by Apache Kafka.

Intelligent Message Syncing: Automatic "catch-up" logic that synchronizes missed messages upon reconnection.

Remote Data Sanitization: Server-triggered "Remote Wipe" logic that surgically clears sensitive local data (IndexedDB) when access is revoked.

Live Presence & Status: Real-time tracking of online/offline status and message lifecycle (Sent/Delivered/Read receipts).

Group Management: Robust group chat orchestration with dynamic key rotation and administrative controls.
