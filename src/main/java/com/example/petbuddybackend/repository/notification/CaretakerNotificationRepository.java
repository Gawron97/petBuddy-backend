package com.example.petbuddybackend.repository.notification;

import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CaretakerNotificationRepository extends JpaRepository<CaretakerNotification, Long> {

    Page<CaretakerNotification> getCaretakerNotificationByCaretaker_EmailAndIsRead(String caretaker_email, boolean isRead,
                                                                                 Pageable pageable);

    @Modifying
    @Query("""
        UPDATE CaretakerNotification n
        SET n.isRead = true
        WHERE n.caretaker.email = :caretaker_email
            AND n.isRead = false

    """)
    void markAllNotificationsOfCaretakerAsRead(String caretaker_email);

}
