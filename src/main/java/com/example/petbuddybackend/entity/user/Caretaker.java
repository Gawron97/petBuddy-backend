package com.example.petbuddybackend.entity.user;

import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.rating.Rating;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "email")
public class Caretaker {

    @Id
    private String email;

    @Column(nullable = false, length = 14)
    private String phoneNumber;

    @Column(length = 1500)
    private String description;

    @PrimaryKeyJoinColumn
    @OneToOne(mappedBy = "caretaker", cascade = CascadeType.MERGE, optional = false)
    private AppUser accountData;

    @Basic(fetch = FetchType.LAZY)
    @Formula("(SELECT COUNT(*) FROM Rating r WHERE r.caretaker_email = email)")
    private Integer numberOfRatings;

    @Basic(fetch = FetchType.LAZY)
    @Formula("(SELECT AVG(r.rating) FROM Rating r WHERE r.caretaker_email = email)")
    private Float avgRating;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "caretaker", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "caretaker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Offer> offers = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.LAZY)
    private Address address;

    @OneToMany(mappedBy = "caretaker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<CaretakerNotification> notifications = new HashSet<>();

}
