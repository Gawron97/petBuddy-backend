package com.example.petbuddybackend.testutils;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.availability.Availability;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.care.CareStatus;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.notification.CaretakerNotification;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.rating.Rating;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.repository.chat.ChatMessageRepository;
import com.example.petbuddybackend.repository.chat.ChatRoomRepository;
import com.example.petbuddybackend.repository.notification.CaretakerNotificationRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.repository.rating.RatingRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import com.example.petbuddybackend.testutils.mock.MockChatProvider;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static com.example.petbuddybackend.testutils.mock.MockCareProvider.createMockCare;
import static com.example.petbuddybackend.testutils.mock.MockNotificationProvider.createMockCaretakerNotification;
import static com.example.petbuddybackend.testutils.mock.MockOfferProvider.*;
import static com.example.petbuddybackend.testutils.mock.MockRatingProvider.createMockRatingForCaretaker;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.*;

public class PersistenceUtils {

    public static AppUser addAppUser(AppUserRepository appUserRepository) {
        AppUser appUser = createMockAppUser();
        return appUserRepository.save(appUser);
    }

    public static AppUser addAppUser(AppUserRepository appUserRepository, AppUser appUser) {
        return appUserRepository.saveAndFlush(appUser);
    }

    public static List<Caretaker> addCaretakers(CaretakerRepository caretakerRepository, AppUserRepository appUserRepository, List<Caretaker> caretakers) {
        List<AppUser> accountData = caretakers.stream()
                .map(Caretaker::getAccountData)
                .toList();

        accountData = appUserRepository.saveAllAndFlush(accountData);

        for (int i = 0; i < caretakers.size(); i++) {
            caretakers.get(i).setEmail(accountData.get(i).getEmail());
        }

        return caretakerRepository.saveAllAndFlush(caretakers);
    }

    public static List<Caretaker> addCaretakers(CaretakerRepository caretakerRepository, AppUserRepository appUserRepository) {
        List<Caretaker> caretakers = createMockCaretakers();

        List<AppUser> accountData = caretakers.stream()
                .map(Caretaker::getAccountData)
                .toList();

        accountData = appUserRepository.saveAllAndFlush(accountData);

        for (int i = 0; i < caretakers.size(); i++) {
            caretakers.get(i).setEmail(accountData.get(i).getEmail());
        }

        return caretakerRepository.saveAllAndFlush(caretakers);
    }

    public static Caretaker addCaretaker(CaretakerRepository caretakerRepository, AppUserRepository appUserRepository, Caretaker caretaker) {
        AppUser caretakerAccountData = appUserRepository.saveAndFlush(caretaker.getAccountData());
        caretaker.setEmail(caretakerAccountData.getEmail());
        return caretakerRepository.saveAndFlush(caretaker);
    }

    public static Caretaker addCaretaker(CaretakerRepository caretakerRepository, AppUserRepository appUserRepository) {
        Caretaker caretaker = createMockCaretaker();
        return addCaretaker(caretakerRepository, appUserRepository, caretaker);
    }

    public static void addOffersToCaretakers(List<Caretaker> caretakers, OfferRepository offerRepository,
                                                        List<Animal> animals) {

        offerRepository.saveAllAndFlush(createMockOffers(caretakers, animals));

    }

    public static Client addClient(AppUserRepository appUserRepository, ClientRepository clientRepository, Client client) {
        AppUser clientAccountData = appUserRepository.saveAndFlush(client.getAccountData());
        client.setEmail(clientAccountData.getEmail());
        return clientRepository.saveAndFlush(client);
    }

    public static Client addClient(AppUserRepository appUserRepository, ClientRepository clientRepository) {
        Client client = createMockClient();
        return addClient(appUserRepository, clientRepository, client);
    }

    public static Offer addComplexOffer(Caretaker caretaker,
                                        Animal animal,
                                        List<AnimalAttribute> animalAttributes,
                                        BigDecimal price,
                                        List<AnimalAmenity> animalAmenities,
                                        Set<Availability> availabilities,
                                        OfferRepository offerRepository) {
        Offer offer = addComplexMockOfferForCaretaker(caretaker, animal, animalAttributes, price, animalAmenities,
                availabilities);
        return offerRepository.save(offer);
    }

    public static Offer addConfigurationAndAmenitiesForOffer(Offer offer, List<AnimalAttribute> animalAttributes, BigDecimal price,
                                                             List<AnimalAmenity> animalAmenities, OfferRepository offerRepository) {
        Offer offerToSave = addConfigurationAndAmenitiesForMockOffer(offer, animalAttributes, price, animalAmenities);
        return offerRepository.save(offerToSave);
    }

    public static void addOfferConfigurationForOffer(Offer existingOffer, List<AnimalAttribute> animalAttributes, OfferRepository offerRepository) {

        OfferConfiguration offerConfiguration = createOfferConfiguration(existingOffer, animalAttributes);
        existingOffer.getOfferConfigurations().add(offerConfiguration);
        offerRepository.save(existingOffer);
    }

    public static ChatRoom addChatRoom(
            ChatRoom chatRoom,
            List<ChatMessage> chatMessages,
            ChatRoomRepository chatRoomRepository,
            ChatMessageRepository chatMessageRepository
    ) {
        chatRoom = chatRoomRepository.saveAndFlush(chatRoom);

        for (ChatMessage chatMessage : chatMessages) {
            chatMessage.setChatRoom(chatRoom);
        }

        chatMessageRepository.saveAllAndFlush(chatMessages);
        chatRoom.setMessages(chatMessages);
        return chatRoom;
    }

    public static Care addCare(CareRepository careRepository, Caretaker caretaker, Client client, Animal animal) {
        Care care = createMockCare(caretaker, client, animal);
        return careRepository.saveAndFlush(care);
    }

    public static Care addCare(CareRepository careRepository, Caretaker caretaker, Client client, Animal animal,
                               ZonedDateTime submittedAt, LocalDate careStart, LocalDate careEnd, BigDecimal dailyPrice,
                               CareStatus caretakerStatus, CareStatus clientStatus) {
        Care care = createMockCare(caretaker, client, animal, submittedAt, careStart, careEnd, dailyPrice,
                caretakerStatus, clientStatus);
        return careRepository.saveAndFlush(care);
    }

    public static Care addCare(CareRepository careRepository, Care care) {
        return careRepository.saveAndFlush(care);
    }

    public static ChatRoom createChatRoomWithMessages(
            AppUserRepository appUserRepository,
            ClientRepository clientRepository,
            CaretakerRepository caretakerRepository,
            ChatRoomRepository chatRepository,
            ChatMessageRepository chatMessageRepository,
            String clientEmail,
            String caretakerEmail,
            ZonedDateTime createdAt
    ) {
        Caretaker caretaker = PersistenceUtils.addCaretaker(
                caretakerRepository,
                appUserRepository,
                MockUserProvider.createMockCaretaker(caretakerEmail)
        );

        Client client = PersistenceUtils.addClient(
                appUserRepository,
                clientRepository,
                MockUserProvider.createMockClient(clientEmail)
        );

        List<ChatMessage> messages = List.of(
                MockChatProvider.createMockChatMessage(client.getAccountData(), createdAt),
                MockChatProvider.createMockChatMessage(caretaker.getAccountData(), createdAt)
        );

        return PersistenceUtils.addChatRoom(
                MockChatProvider.createMockChatRoom(client, caretaker),
                messages,
                chatRepository,
                chatMessageRepository
        );
    }

    public static ChatRoom createChatRoomWithMessages(
            AppUserRepository appUserRepository,
            ClientRepository clientRepository,
            CaretakerRepository caretakerRepository,
            ChatRoomRepository chatRepository,
            ChatMessageRepository chatMessageRepository,
            String clientEmail,
            String caretakerEmail
    ) {
        Caretaker caretaker = PersistenceUtils.addCaretaker(
                caretakerRepository,
                appUserRepository,
                MockUserProvider.createMockCaretaker(caretakerEmail)
        );

        Client client = PersistenceUtils.addClient(
                appUserRepository,
                clientRepository,
                MockUserProvider.createMockClient(clientEmail)
        );

        List<ChatMessage> messages = List.of(
                MockChatProvider.createMockChatMessage(client.getAccountData(), ZonedDateTime.now().minusDays(2)),
                MockChatProvider.createMockChatMessage(caretaker.getAccountData(), ZonedDateTime.now().minusDays(1))
        );

        return PersistenceUtils.addChatRoom(
                MockChatProvider.createMockChatRoom(client, caretaker),
                messages,
                chatRepository,
                chatMessageRepository
        );
    }

    public static Rating addRatingToCaretaker(Caretaker caretaker, Client client, Care care, Integer ratingNumber, String comment,
                                              RatingRepository ratingRepository) {
        Rating rating = createMockRatingForCaretaker(caretaker, client, care, ratingNumber, comment);
        return ratingRepository.save(rating);
    }

    public static Rating addRatingToCaretaker(RatingRepository ratingRepository, Rating rating) {
        return ratingRepository.saveAndFlush(rating);
    }

    public static void setAvailabilitiesForOffer(OfferRepository offerRepository, Offer existingOffer) {
        Offer offer = setMockAvailabilitiesToOffer(existingOffer);
        offerRepository.save(offer);
    }

    public static void setAvailabilitiesForOffer(OfferRepository offerRepository, Offer existingOffer,
                                                 Set<Availability> availabilities) {
        Offer offer = setAvailabilitiesToOffer(existingOffer, availabilities);
        offerRepository.save(offer);
    }

    public static void addFollowingCaretakersToClient(ClientRepository clientRepository, Client client,
                                                       Set<Caretaker> caretakers) {
        client.getFollowingCaretakers().addAll(caretakers);
        clientRepository.saveAndFlush(client);
    }

    public static CaretakerNotification addCaretakerNotification(CaretakerNotificationRepository caretakerNotificationRepository,
                                                                 Caretaker caretaker) {
        CaretakerNotification notification = createMockCaretakerNotification(caretaker);
        return caretakerNotificationRepository.saveAndFlush(notification);
    }

}
