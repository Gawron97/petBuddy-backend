package com.example.petbuddybackend.entity.rating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingKey implements Serializable {
    private String caretakerEmail;
    private String clientEmail;
    private Long careId;
}
