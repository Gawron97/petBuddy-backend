package com.example.petbuddybackend.service.care.state;

import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.user.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RoleTransition extends Transition {

    private Role role;

    public RoleTransition(Role role, CareStatus fromStatus, CareStatus toStatus) {
        super(fromStatus, toStatus);
        this.role = role;
    }
}
