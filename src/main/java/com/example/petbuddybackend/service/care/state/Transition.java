package com.example.petbuddybackend.service.care.state;

import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Role;

public record Transition(Role role, CareStatus fromStatus, CareStatus toStatus) {
}
