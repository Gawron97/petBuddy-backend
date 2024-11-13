package com.example.petbuddybackend.entity.rating;


import com.example.petbuddybackend.entity.care.Care;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;


@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Check(constraints = "rating >= 1 AND rating <= 5")
public class Rating {

    @Id
    private Long careId;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 500)
    private String comment;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "careId", referencedColumnName = "id", updatable = false)
    private Care care;

    @PrePersist
    public void prePersist() {
        if(care.getCaretaker().getEmail().equals(care.getClient().getEmail())) {
            throw new IllegalStateException("Caretaker and client cannot be the same person");
        }
    }
}
