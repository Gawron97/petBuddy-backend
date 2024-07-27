package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.repository.ClientRepository;
import com.example.petbuddybackend.utils.exception.throweable.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public Long getClientIdByUsername(String username) {
        return clientRepository.findClientIdByUsername(username)
                .orElseThrow(() -> new NotFoundException("Client with username " + username + " does not exist"));
    }
}
