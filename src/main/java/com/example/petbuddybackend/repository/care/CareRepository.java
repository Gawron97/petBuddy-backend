package com.example.petbuddybackend.repository.care;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CareRepository extends JpaRepository<Care, Long> {

    List<Care> findAllByCaretakerStatusNotInOrClientStatusNotIn(Collection<CareStatus> caretakerStatus,
                                                                Collection<CareStatus> clientStatus);

}
