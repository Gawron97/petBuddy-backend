package com.example.petbuddybackend.repository.care;

import com.example.petbuddybackend.dto.user.SimplifiedAccountDataDTO;
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

import java.time.LocalDate;
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
            AND c.careStart < :threshold
            """
    )
    int outdateCaresBetweenClientAndCaretaker(Collection<CareStatus> statusesPrerequisites, LocalDate threshold);

    @Query("""
            SELECT new com.example.petbuddybackend.dto.user.SimplifiedAccountDataDTO(
                c.client.email,
                c.client.accountData.name,
                c.client.accountData.surname
            )
            FROM Care c
            WHERE c.caretaker.email = :caretakerEmail
            """
    )
    Page<SimplifiedAccountDataDTO> findClientsRelatedToYourCares(String caretakerEmail, Pageable pageable);

    @Query("""
            SELECT new com.example.petbuddybackend.dto.user.SimplifiedAccountDataDTO(
                c.caretaker.email,
                c.caretaker.accountData.name,
                c.caretaker.accountData.surname
            )
            FROM Care c
            WHERE c.client.email = :clientEmail
            """
    )
    Page<SimplifiedAccountDataDTO> findCaretakersRelatedToYourCares(String clientEmail, Pageable pageable);

    @Query("""
            SELECT c
            FROM Care c
            WHERE c.careStart = current_date
            """
    )
    List<Care> findAllCaresWithStartDateToday();

}

