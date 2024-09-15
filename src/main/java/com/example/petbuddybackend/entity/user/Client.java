package com.example.petbuddybackend.entity.user;

import com.example.petbuddybackend.entity.rating.Rating;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    @Id
    private String email;

    @PrimaryKeyJoinColumn
    @OneToOne(cascade = CascadeType.MERGE, optional = false)
    private AppUser accountData;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "client", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Rating> ratings = new ArrayList<>();
}
