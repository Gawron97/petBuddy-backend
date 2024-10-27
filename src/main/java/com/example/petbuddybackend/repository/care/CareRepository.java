package com.example.petbuddybackend.repository.care;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface CareRepository extends JpaRepository<Care, Long>, JpaSpecificationExecutor<Care> {

    List<Care> findAllByCaretakerStatusNotInOrClientStatusNotIn(Collection<CareStatus> caretakerStatus,
                                                                Collection<CareStatus> clientStatus);

    Page<Care> findAll(Specification<Care> spec, Pageable pageable);

    @Transactional
    @Modifying
    @Query("""
            UPDATE Care c SET
                c.caretakerStatus = com.example.petbuddybackend.entity.care.CareStatus.CANCELLED,
                c.clientStatus = com.example.petbuddybackend.entity.care.CareStatus.CANCELLED
            WHERE c.client.email = :clientEmail
            AND c.caretaker.email = :caretakerEmail
            AND c.caretakerStatus IN :statusesPrerequisites
            AND c.clientStatus IN :statusesPrerequisites
            """
    )
    void cancelCaresBetweenClientAndCaretaker(
            String clientEmail,
            String caretakerEmail,
            Collection<CareStatus> statusesPrerequisites
    );

    @Transactional
    @Modifying
    @Query("""
            UPDATE Care c SET
                c.caretakerStatus = com.example.petbuddybackend.entity.care.CareStatus.OUTDATED,
                c.clientStatus = com.example.petbuddybackend.entity.care.CareStatus.OUTDATED
            WHERE c.caretakerStatus IN :statusesPrerequisites
            AND c.clientStatus IN :statusesPrerequisites
            AND c.careEnd < CURRENT_DATE
            """
    )
    int outdateCaresBetweenClientAndCaretaker(Collection<CareStatus> statusesPrerequisites);

}
