package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.user.ClientComplexInfoDTO;
import com.example.petbuddybackend.dto.user.ClientDTO;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.service.mapper.ClientMapper;
import com.example.petbuddybackend.service.photo.PhotoService;
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

    public boolean clientExists(String clientEmail) {
        return clientRepository.existsById(clientEmail);
    }

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

    public ClientComplexInfoDTO addFollowingCaretakers(String clientEmail, Set<String> caretakerEmails) {

        Client client = getClientByEmail(clientEmail);
        assertClientNotFollowingItself(client, caretakerEmails);
        assertNewFollowingCaretakersAreNotAlreadyFollowed(client, caretakerEmails);

        Set<Caretaker> caretakersToFollow = getCaretakersByEmails(caretakerEmails);
        client.getFollowingCaretakers().addAll(caretakersToFollow);
        return clientMapper.mapToClientComplexInfoDTO(clientRepository.save(client));
    }

    public ClientComplexInfoDTO removeFollowingCaretakers(String clientEmail, Set<String> caretakerEmails) {

        Client client = getClientByEmail(clientEmail);
        assertClientNotFollowingItself(client, caretakerEmails);
        assertCaretakersToRemoveAreFollowed(client, caretakerEmails);

        Set<Caretaker> caretakersToUnfollow = getCaretakersByEmails(caretakerEmails);
        client.getFollowingCaretakers().removeAll(caretakersToUnfollow);
        return clientMapper.mapToClientComplexInfoDTO(clientRepository.save(client));
    }

    private Client createClient(JwtAuthenticationToken token) {

        AppUser appUser = userService.createUserIfNotExistOrGet(token);

        return Client.builder()
                .email(appUser.getEmail())
                .accountData(appUser)
                .build();

    }

    private void assertClientNotFollowingItself(Client client, Set<String> caretakerEmails) {
        if(caretakerEmails.stream().anyMatch(caretakerEmail -> caretakerEmail.equals(client.getEmail()))) {
            throw new IllegalActionException("Client cannot follow itself");
        }
    }

    private void assertNewFollowingCaretakersAreNotAlreadyFollowed(Client client, Set<String> caretakerEmails) {
        Set<String> alreadyFollowedCaretakersEmails = getFollowedCaretakersEmails(client);

        Set<String> conflicts = alreadyFollowedCaretakersEmails
                .stream()
                .filter(caretakerEmails::contains)
                .collect(Collectors.toSet());

        if(!conflicts.isEmpty()) {
            throw new IllegalActionException("Client is already following caretakers: " + conflicts);
        }

    }

    private Set<Caretaker> getCaretakersByEmails(Set<String> caretakerEmails) {
        return caretakerEmails
                .stream()
                .map(this::getCaretakerByEmail)
                .collect(Collectors.toSet());
    }

    private Caretaker getCaretakerByEmail(String caretakerEmail) {
        return caretakerRepository.findById(caretakerEmail)
                .orElseThrow(() -> NotFoundException.withFormattedMessage(CARETAKER, caretakerEmail));
    }

    private void assertCaretakersToRemoveAreFollowed(Client client, Set<String> caretakerEmails) {

        Set<String> alreadyFollowedCaretakersEmails = getFollowedCaretakersEmails(client);

        Set<String> notFollowed = caretakerEmails
                .stream()
                .filter(caretakerEmail -> !alreadyFollowedCaretakersEmails.contains(caretakerEmail))
                .collect(Collectors.toSet());

        if(!notFollowed.isEmpty()) {
            throw new IllegalActionException("Client is trying to unfollow not followed caretakers: " + notFollowed);
        }

    }

    private Set<String> getFollowedCaretakersEmails(Client client) {
        return client.getFollowingCaretakers()
                .stream()
                .map(Caretaker::getEmail)
                .collect(Collectors.toSet());
    }

}
