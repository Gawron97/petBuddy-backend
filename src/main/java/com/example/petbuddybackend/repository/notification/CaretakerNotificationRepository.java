package com.example.petbuddybackend.repository.notification;

import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaretakerNotificationRepository extends JpaRepository<CaretakerNotification, Long> {

    Page<CaretakerNotification> getCaretakerNotificationByCaretaker_EmailAndIsRead(String caretaker_email, boolean isRead,
                                                                                 Pageable pageable);

}
