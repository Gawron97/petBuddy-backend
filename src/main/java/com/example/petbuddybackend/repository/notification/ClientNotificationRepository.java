package com.example.petbuddybackend.repository.notification;

import com.example.petbuddybackend.entity.notification.ClientNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ClientNotificationRepository extends JpaRepository<ClientNotification, Long> {

    Page<ClientNotification> getClientNotificationByClient_Email(String client_email, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ClientNotification n
        SET n.read = true
        WHERE n.client.email = :clientEmail
            AND n.read = false

    """)
    void markAllNotificationsOfClientAsRead(String clientEmail);
}
