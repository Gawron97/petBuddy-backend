package com.example.petbuddybackend.repository.care;

import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface CareRepository extends JpaRepository<Care, Long>, JpaSpecificationExecutor<Care> {

    List<Care> findAllByCaretakerStatusNotInOrClientStatusNotIn(Collection<CareStatus> caretakerStatus,
                                                                Collection<CareStatus> clientStatus);

    Page<Care> findAll(Specification<Care> spec, Pageable pageable);

}
