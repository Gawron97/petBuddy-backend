package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.user.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserService userService;

    public boolean clientExists(String clientEmail) {
        return clientRepository.existsById(clientEmail);
    }

    @Transactional
    public void createClientIfNotExist(JwtAuthenticationToken token) {
        String email = (String) token.getTokenAttributes().get("email");

        if(!clientExists(email)) {
            log.info("Creating client with email: " + email);
            Client client = createClient(token);
            clientRepository.save(client);
            log.info("Client with email created: " + client.getEmail());
        }
    }

    private Client createClient(JwtAuthenticationToken token) {

        AppUser appUser = userService.createUserIfNotExistOrGet(token);

        return Client.builder()
                .email(appUser.getEmail())
                .accountData(appUser)
                .build();

    }

}
