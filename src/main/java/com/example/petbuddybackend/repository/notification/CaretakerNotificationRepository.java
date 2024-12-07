package com.example.petbuddybackend.repository.notification;

import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface CaretakerNotificationRepository extends JpaRepository<CaretakerNotification, Long> {

    Page<CaretakerNotification> getCaretakerNotificationByCaretaker_Email(String caretaker_email, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        UPDATE CaretakerNotification n
        SET n.isRead = true
        WHERE n.caretaker.email = :caretakerEmail
            AND n.isRead = false

    """)
    void markAllNotificationsOfCaretakerAsRead(String caretakerEmail);

}
