package com.example.petbuddybackend.service.care.state;

import com.example.petbuddybackend.entity.care.CareStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transition {
    private CareStatus fromStatus;
    private CareStatus toStatus;
}
