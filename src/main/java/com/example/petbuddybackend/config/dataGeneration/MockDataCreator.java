package com.example.petbuddybackend.config.dataGeneration;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.CareRepository;
import com.example.petbuddybackend.repository.amenity.AnimalAmenityRepository;
import com.example.petbuddybackend.repository.animal.AnimalAttributeRepository;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.chat.ChatMessageRepository;
import com.example.petbuddybackend.repository.chat.ChatRoomRepository;
import com.example.petbuddybackend.repository.offer.OfferConfigurationRepository;
import com.example.petbuddybackend.repository.offer.OfferOptionRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.service.dataGeneration.MockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
@Slf4j
@Profile("dev")
@RequiredArgsConstructor
public class MockDataCreator {

    private static final int CARETAKER_COUNT = 50;
    private static final int CLIENT_COUNT = 50;
    private static final int CARETAKER_OFFER_COUNT = 5;
    private static final int CARETAKER_OFFER_CONFIGURATION_COUNT = 2;
    private static final int OPTIONS_IN_CONFIGURATION_COUNT = 2;
    private static final int ANIMAL_AMENITY_IN_OFFER_COUNT = 2;
    private static final int CARE_COUNT = 30;

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
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CareRepository careRepository;

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

        addRatingsToSliceOfCaretakers(caretakers, clients);


        // caretaker offers
        animals = animalRepository.findAll();
        animalAttributes = animalAttributeRepository.findAll();
        offers = offerRepository.saveAllAndFlush(mockService.createMockOffers(caretakers, animals, CARETAKER_OFFER_COUNT));
        offerConfigurationRepository.saveAllAndFlush(
                        mockService.createMockOffersConfigurations(offers, CARETAKER_OFFER_CONFIGURATION_COUNT));

        // offers configurations options
        caretakers = caretakerRepository.findAll();
        caretakers.get(0).getOffers().get(0).getOfferConfigurations(); // to fetch lazy lists
        offerOptionRepository.saveAllAndFlush(
                mockService.createMockOfferConfigurationsOptionsForCaretakers(caretakers, animalAttributes, OPTIONS_IN_CONFIGURATION_COUNT));

        // offers amenities
        offers = offerRepository.findAll();
        animalAmenities = animalAmenityRepository.findAll();
        offerRepository.saveAllAndFlush(mockService.createMockOffersAmenities(offers, animalAmenities, ANIMAL_AMENITY_IN_OFFER_COUNT));

        // chat
        Client client = createKnownClient();
        createChat(client, caretakers.get(0));

        // cares
        careRepository.saveAllAndFlush(mockService.createMockCares(clients, caretakers, animals, animalAttributes, CARE_COUNT));


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
        return caretakerRepository.count() != 0 &&
                appUserRepository.count() != 0 &&
                ratingRepository.count() != 0 &&
                offerRepository.count() != 0 &&
                offerConfigurationRepository.count() != 0 &&
                offerOptionRepository.count() != 0 &&
                animalAmenityRepository.count() != 0 &&
                chatRoomRepository.count() != 0 &&
                chatMessageRepository.count() != 0 &&
                careRepository.count() != 0;
    }

    private List<Rating> addRatingsToSliceOfCaretakers(List<Caretaker> allCaretakers, List<Client> clients) {
        int caretakersWithRatingsCount = (int)(allCaretakers.size() * 0.7f);
        List<Caretaker> caretakersWithRatings = caretakersWithRatingsCount == 0 ?
                allCaretakers :
                allCaretakers.subList(0, caretakersWithRatingsCount);

        return ratingRepository.saveAllAndFlush(mockService.createMockRatings(caretakersWithRatings, clients));
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

    private void createChat(Client client, Caretaker caretaker) {
        ChatRoom chatRoom = ChatRoom.builder()
                .client(client)
                .caretaker(caretaker)
                .build();

        chatRoom = chatRoomRepository.save(chatRoom);
        List<ChatMessage> messages = mockService.generateMessages(10, client, caretaker, chatRoom);

        chatMessageRepository.saveAllAndFlush(messages);
    }
}
