package com.example.petbuddybackend.repository.notification;

import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaretakerNotificationRepository extends JpaRepository<CaretakerNotification, Long> {
}
