package com.example.petbuddybackend.repository.care;

import com.example.petbuddybackend.entity.care.CareStatusesHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareStatusesHistoryRepository extends JpaRepository<CareStatusesHistory, Long> {
}
