package com.example.petbuddybackend.entity.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    @Id
    private Long id;

    @PrimaryKeyJoinColumn
    @OneToOne(cascade = CascadeType.MERGE, optional = false)
    private AppUser accountData;
}
