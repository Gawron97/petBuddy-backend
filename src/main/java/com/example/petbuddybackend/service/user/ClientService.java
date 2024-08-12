package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public boolean clientExists(String clientEmail) {
        return clientRepository.existsById(clientEmail);
    }
}
