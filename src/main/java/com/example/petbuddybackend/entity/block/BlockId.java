package com.example.petbuddybackend.entity.block;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockId implements Serializable {
    private String blockerEmail;
    private String blockedEmail;
}
