package com.example.petbuddybackend.repository.notification;

import com.example.petbuddybackend.entity.notification.ClientNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ClientNotificationRepository extends JpaRepository<ClientNotification, Long> {

    Page<ClientNotification> getClientNotificationByClient_EmailAndIsRead(String client_email, boolean isRead,
                                                                        Pageable pageable);

    @Modifying
    @Query("""
        UPDATE ClientNotification n
        SET n.isRead = true
        WHERE n.client.email = :client_email
            AND n.isRead = false

    """)
    void markAllNotificationsOfClientAsRead(String client_email);
}
