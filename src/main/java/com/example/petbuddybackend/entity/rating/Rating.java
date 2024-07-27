package com.example.petbuddybackend.entity.rating;


import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;


@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@IdClass(RatingKey.class)
@Check(constraints = "rating >= 1 AND rating <= 5")
public class Rating {

    @Id
    private Long clientId;

    @Id
    private Long caretakerId;

    @Column(nullable = false)
    private int rating;

    @Column(length = 500)
    private String comment;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clientId", referencedColumnName = "id", updatable = false)
    private Client client;


    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caretakerId", referencedColumnName = "id", updatable = false)
    private Caretaker caretaker;
}
