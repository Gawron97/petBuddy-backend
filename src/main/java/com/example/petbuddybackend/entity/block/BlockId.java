package com.example.petbuddybackend.entity.block;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Builder
public class BlockId implements Serializable {
    private String blockerEmail;
    private String blockedEmail;
}
