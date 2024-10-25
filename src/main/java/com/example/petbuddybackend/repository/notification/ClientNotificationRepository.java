package com.example.petbuddybackend.repository.notification;

import com.example.petbuddybackend.entity.notification.ClientNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientNotificationRepository extends JpaRepository<ClientNotification, Long> {

    Page<ClientNotification> getClientNotificationByClient_EmailAndIsRead(String client_email, boolean isRead,
                                                                        Pageable pageable);
}
