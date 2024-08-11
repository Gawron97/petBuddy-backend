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
@Check(constraints = "rating >= 1 AND rating <= 5 AND client_email <> caretaker_email")
public class Rating {

    @Id
    private String clientEmail;

    @Id
    private String caretakerEmail;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 500)
    private String comment;

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clientEmail", referencedColumnName = "email", updatable = false)
    private Client client;


    @MapsId
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "caretakerEmail", referencedColumnName = "email", updatable = false)
    private Caretaker caretaker;
}
