package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.service.mapper.ClientMapper;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private static final String CLIENT = "Client";

    private final ClientRepository clientRepository;
    private final UserService userService;

    private final ClientMapper clientMapper = ClientMapper.INSTANCE;

    public boolean clientExists(String clientEmail) {
        return clientRepository.existsById(clientEmail);
    }

    public Client getClientByEmail(String clientEmail) {
        return clientRepository.findById(clientEmail)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(clientEmail, CLIENT));
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

    public ClientDTO getClient(String clientEmail) {
        return clientMapper.mapToClientDTO(getClientByEmail(clientEmail));
    }
}
