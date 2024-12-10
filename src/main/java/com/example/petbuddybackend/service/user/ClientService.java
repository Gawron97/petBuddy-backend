package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.service.mapper.CaretakerMapper;
import com.example.petbuddybackend.service.mapper.ClientMapper;
import com.example.petbuddybackend.service.mapper.UserMapper;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private static final String CLIENT = "Client";
    private static final String CARETAKER = "Caretaker";

    private final ClientRepository clientRepository;
    private final CaretakerRepository caretakerRepository;
    private final UserService userService;
    private final ClientMapper clientMapper = ClientMapper.INSTANCE;
    private final UserMapper userMapper = UserMapper.INSTANCE;
    private final CaretakerMapper caretakerMapper = CaretakerMapper.INSTANCE;

    public Client getClientByEmail(String clientEmail) {
        return clientRepository.findById(clientEmail)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(CLIENT, clientEmail));
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

    public ClientDTO getClient(String clientEmail) {
        Client client = getClientByEmail(clientEmail);
        userService.renewProfilePicture(client.getAccountData());
        return clientMapper.mapToClientDTO(client);
    }

    public Set<String> addFollowingCaretaker(String clientEmail, String caretakerEmail) {

        assertClientNotFollowingItself(clientEmail, caretakerEmail);
        Client client = getClientByEmail(clientEmail);
        assertCaretakerIsNotAlreadyFollowed(client, caretakerEmail);

        Caretaker caretakerToFollow = getCaretakerByEmail(caretakerEmail);
        client.getFollowingCaretakers().add(caretakerToFollow);
        clientRepository.save(client);

        return getFollowedCaretakersEmails(client);
    }

    public Set<String> removeFollowingCaretaker(String clientEmail, String caretakerEmail) {

        Client client = getClientByEmail(clientEmail);
        assertCaretakerToRemoveIsFollowed(client, caretakerEmail);

        Caretaker caretakerToUnfollow = getCaretakerByEmail(caretakerEmail);
        client.getFollowingCaretakers().remove(caretakerToUnfollow);
        clientRepository.save(client);

        return getFollowedCaretakersEmails(client);
    }

    public Set<CaretakerDTO> getFollowedCaretakers(String clientEmail) {
        Client client = getClientByEmail(clientEmail);
        return client.getFollowingCaretakers()
                .stream()
                .peek(caretaker -> userService.renewProfilePicture(caretaker.getAccountData()))
                .map(caretakerMapper::mapToCaretakerDTO)
                .collect(Collectors.toSet());
    }

    private Client createClient(JwtAuthenticationToken token) {

        AppUser appUser = userService.createUserIfNotExistOrGet(token);

        return Client.builder()
                .email(appUser.getEmail())
                .accountData(appUser)
                .build();

    }

    private void assertClientNotFollowingItself(String clientEmail, String caretakerEmail) {
        if(clientEmail.equals(caretakerEmail)) {
            throw new IllegalActionException("Client cannot follow itself");
        }
    }

    private void assertCaretakerIsNotAlreadyFollowed(Client client, String caretakerEmail) {
        if(client.getFollowingCaretakers().stream().anyMatch(caretaker -> caretaker.getEmail().equals(caretakerEmail))) {
            throw new IllegalActionException("Client is already following caretaker: " + caretakerEmail);
        }
    }

    private void assertCaretakerToRemoveIsFollowed(Client client, String caretakerEmail) {
        if(client.getFollowingCaretakers().stream().noneMatch(caretaker -> caretaker.getEmail().equals(caretakerEmail))) {
            throw new IllegalActionException("Client is trying to unfollow not followed caretaker: " + caretakerEmail);
        }
    }

    private Caretaker getCaretakerByEmail(String caretakerEmail) {
        return caretakerRepository.findById(caretakerEmail)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(CARETAKER, caretakerEmail));
    }

    private boolean clientExists(String clientEmail) {
        return clientRepository.existsById(clientEmail);
    }

    private Set<String> getFollowedCaretakersEmails(Client client) {
        return client.getFollowingCaretakers()
                .stream()
                .map(Caretaker::getEmail)
                .collect(Collectors.toSet());
    }
}
