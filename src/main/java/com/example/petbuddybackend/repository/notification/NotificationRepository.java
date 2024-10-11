package com.example.petbuddybackend.repository.notification;

import com.example.petbuddybackend.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
