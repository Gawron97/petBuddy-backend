package com.example.petbuddybackend.repository.notification;

import com.example.petbuddybackend.entity.notification.ClientNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientNotificationRepository extends JpaRepository<ClientNotification, Long> {
}
