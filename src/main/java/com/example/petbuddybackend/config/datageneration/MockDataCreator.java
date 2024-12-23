package com.example.petbuddybackend.config.datageneration;

import com.example.petbuddybackend.dto.chat.ChatMessageSent;
import com.example.petbuddybackend.dto.chat.ChatRoomDTO;
import com.example.petbuddybackend.dto.criteriaSearch.ChatRoomSearchCriteria;
import com.example.petbuddybackend.entity.address.Address;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.care.CareStatusesHistory;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.repository.amenity.AnimalAmenityRepository;
import com.example.petbuddybackend.repository.animal.AnimalAttributeRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.availability.AvailabilityRepository;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.repository.care.CareStatusesHistoryRepository;
import com.example.petbuddybackend.repository.chat.ChatMessageRepository;
import com.example.petbuddybackend.repository.chat.ChatRoomRepository;
import com.example.petbuddybackend.repository.offer.OfferConfigurationRepository;
import com.example.petbuddybackend.repository.offer.OfferOptionRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.service.chat.ChatService;
import com.example.petbuddybackend.service.datageneration.MockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;


@Component
@Slf4j
@Profile("dev")
@RequiredArgsConstructor
public class MockDataCreator {

    private static final int CARETAKER_COUNT = 50;
    private static final int CLIENT_COUNT = 50;
    private static final int CARETAKER_OFFER_COUNT = 7;
    private static final int OPTIONS_IN_CONFIGURATION_COUNT = 2;
    private static final int ANIMAL_AMENITY_IN_OFFER_COUNT = 2;
    private static final int CARE_COUNT = 300;
    private static final int FOLLOWING_CARETAKERS_COUNT = 7;

    private final MockService mockService;
    private final CaretakerRepository caretakerRepository;
    private final ClientRepository clientRepository;
    private final AppUserRepository appUserRepository;
    private final RatingRepository ratingRepository;
    private final AnimalRepository animalRepository;
    private final AnimalAttributeRepository animalAttributeRepository;
    private final OfferRepository offerRepository;
    private final OfferConfigurationRepository offerConfigurationRepository;
    private final OfferOptionRepository offerOptionRepository;
    private final AnimalAmenityRepository animalAmenityRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CareRepository careRepository;
    private final ChatService chatService;
    private final CareStatusesHistoryRepository careStatusesHistoryRepository;

    List<AppUser> caretakerAppUsers;
    List<AppUser> clientAppUsers;
    List<Client> clients;
    List<Caretaker> caretakers;
    List<Animal> animals;
    List<AnimalAttribute> animalAttributes;
    List<Offer> offers;
    List<AnimalAmenity> animalAmenities;

    @Transactional
    public void createMockData() {
        if (shouldSkipInit()) {
            return;
        }
        log.info("Creating mock data in database...");

        // users
        caretakerAppUsers = appUserRepository.saveAllAndFlush(mockService.createMockAppUsers(CARETAKER_COUNT));
        clientAppUsers = appUserRepository.saveAllAndFlush(mockService.createMockAppUsers(CLIENT_COUNT));

        clients = clientRepository.saveAllAndFlush(mockService.createMockClients(clientAppUsers));
        caretakers = caretakerRepository.saveAllAndFlush(mockService.createMockCaretakers(caretakerAppUsers));

        // caretaker offers
        animals = animalRepository.findAll();
        animalAttributes = animalAttributeRepository.findAll();
        offers = offerRepository.saveAllAndFlush(
                mockService.createMockOffers(caretakers, animals, CARETAKER_OFFER_COUNT)
        );

        // offers configurations
        offerConfigurationRepository.saveAllAndFlush(
                        mockService.createMockOffersConfigurations(
                                offers,
                                animalAttributes,
                                OPTIONS_IN_CONFIGURATION_COUNT
                        )
        );

        // offers configurations options
        caretakers = caretakerRepository.findAll();
        caretakers.get(0).getOffers().get(0).getOfferConfigurations(); // to fetch lazy lists
        offerOptionRepository.saveAllAndFlush(
                mockService.createMockOfferConfigurationsOptionsForCaretakers(
                        caretakers,
                        animalAttributes,
                        OPTIONS_IN_CONFIGURATION_COUNT
                )
        );

        // offers amenities
        offers = offerRepository.findAll();
        animalAmenities = animalAmenityRepository.findAll();
        offerRepository.saveAllAndFlush(
                mockService.createMockOffersAmenities(offers, animalAmenities, ANIMAL_AMENITY_IN_OFFER_COUNT)
        );

        // offers availabilities
        offers = offerRepository.findAll();
        availabilityRepository.saveAllAndFlush(mockService.createMockAvailabilitiesForOffers(offers));

        // cares
        clients = clientRepository.findAll();
        caretakers = caretakerRepository.findAll();
        careRepository.saveAllAndFlush(
                mockService.createMockCares(clients, caretakers, animals, animalAttributes, LocalDate.now(), CARE_COUNT)
        );

        // ratings
        caretakers = caretakerRepository.findAll();
        addRatingsToSliceOfCaretakers(caretakers);

        // following caretakers
        clients = clientRepository.findAll();
        caretakers = caretakerRepository.findAll();
        clientRepository.saveAllAndFlush(
                mockService.addFollowingCaretakersToClients(clients, caretakers, FOLLOWING_CARETAKERS_COUNT)
        );

        createKnownMockUsers();

        // clean cache
        log.info("Mock data created successfully!");
        cleanCache();
    }

    private void cleanCache() {
        caretakerAppUsers.clear();
        clientAppUsers.clear();
        clients.clear();
        caretakers.clear();
        animals.clear();
        animalAttributes.clear();
        offers.clear();
        animalAmenities.clear();
    }

    private boolean shouldSkipInit() {
        return caretakerRepository.count() != 0 ||
                appUserRepository.count() != 0 ||
                ratingRepository.count() != 0 ||
                offerRepository.count() != 0 ||
                offerConfigurationRepository.count() != 0 ||
                offerOptionRepository.count() != 0 ||
                availabilityRepository.count() != 0 ||
                chatRoomRepository.count() != 0 ||
                chatMessageRepository.count() != 0 ||
                careRepository.count() != 0;
    }

    private void createKnownMockUsers() {
        Client client = createKnownClient();
        Caretaker caretaker = createKnownCaretaker();

        Care care = careRepository.saveAndFlush(mockService.createMockCare(
                client,
                caretaker,
                animals.get(0),
                animalAttributes,
                LocalDate.now(),
                CareStatus.READY_TO_PROCEED,
                CareStatus.READY_TO_PROCEED
        ));

        careStatusesHistoryRepository.save(CareStatusesHistory.builder()
                        .clientStatus(CareStatus.READY_TO_PROCEED)
                        .caretakerStatus(CareStatus.READY_TO_PROCEED)
                        .care(care)
                        .build()
        );

        createChat(client, caretaker);
        createChat(client, caretakers.get(1));
    }

    private List<Rating> addRatingsToSliceOfCaretakers(List<Caretaker> allCaretakers) {
        int caretakersWithRatingsCount = (int)(allCaretakers.size() * 0.7f);
        List<Caretaker> caretakersWithRatings = caretakersWithRatingsCount == 0 ?
                allCaretakers :
                allCaretakers.subList(0, caretakersWithRatingsCount);

        return ratingRepository.saveAllAndFlush(mockService.createMockRatings(caretakersWithRatings));
    }

    private Client createKnownClient() {
        AppUser user = AppUser.builder()
                .email("user@backend.com")
                .name("MyName")
                .surname("MySurname")
                .build();

        appUserRepository.save(user);

        Client client = Client.builder()
                .accountData(user)
                .email(user.getEmail())
                .build();

        return clientRepository.save(client);
    }

    private Caretaker createKnownCaretaker() {
        AppUser user = AppUser.builder()
                .email("user@frontend.com")
                .name("MyName")
                .surname("MySurname")
                .build();

        appUserRepository.save(user);

        Address address = Address.builder()
                .city("City")
                .street("Street")
                .zipCode("12345")
                .streetNumber("1")
                .voivodeship(Voivodeship.SLASKIE)
                .latitude(BigDecimal.valueOf(54.00))
                .longitude(BigDecimal.valueOf(18.00))
                .build();

        Caretaker caretaker = Caretaker.builder()
                .accountData(user)
                .phoneNumber("123456789")
                .email(user.getEmail())
                .address(address)
                .build();

        return caretakerRepository.save(caretaker);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChatRoom createChat(Client client, Caretaker caretaker) {
        chatService.createChatRoomWithMessage(
                client.getEmail(),
                Role.CLIENT,
                caretaker.getEmail(),
                new ChatMessageSent("Initial message to caretaker!"),
                ZoneId.systemDefault()
        );

        Page<ChatRoomDTO> chatRooms = chatService.getChatRooms(
                client.getEmail(),
                Role.CLIENT,
                new ChatRoomSearchCriteria(""),
                PageRequest.of(0, 1),
                ZoneId.systemDefault()
        );

        ChatRoomDTO createdChatRoomDTO = chatRooms.getContent().get(0);
        ChatRoom chatRoom = chatRoomRepository.findById(createdChatRoomDTO.getId()).orElseThrow();

        for (int i = 0; i < 10; i++) {
            chatService.createMessage(
                    chatRoom,
                    client.getEmail(),
                    Role.CLIENT,
                    new ChatMessageSent("Message from client: " + i),
                    false
            );
        }
        for (int i = 0; i < 10; i++) {
            chatService.createMessage(
                    chatRoom,
                    caretaker.getEmail(),
                    Role.CARETAKER,
                    new ChatMessageSent("Message from caretaker: " + i),
                    false
            );
        }

        return chatRoom;
    }
}
