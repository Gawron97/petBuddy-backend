package com.example.petbuddybackend.testutils;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.care.Care;
import com.example.petbuddybackend.entity.chat.ChatMessage;
import com.example.petbuddybackend.entity.chat.ChatRoom;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.offer.OfferConfiguration;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.care.CareRepository;
import com.example.petbuddybackend.repository.chat.ChatMessageRepository;
import com.example.petbuddybackend.repository.chat.ChatRoomRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;

import java.math.BigDecimal;
import java.util.List;

import static com.example.petbuddybackend.testutils.mock.MockCareProvider.createMockCare;
import static com.example.petbuddybackend.testutils.mock.MockOfferProvider.*;
import static com.example.petbuddybackend.testutils.mock.MockUserProvider.*;

public class PersistenceUtils {

    public static AppUser addAppUser(AppUserRepository appUserRepository) {
        AppUser appUser = createMockAppUser();
        return appUserRepository.save(appUser);
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

    public static AppUser addAppUser(AppUserRepository appUserRepository, AppUser appUser) {
        return appUserRepository.saveAndFlush(appUser);
    }

    public static Offer addComplexOffer(Caretaker caretaker, Animal animal, List<AnimalAttribute> animalAttributes,
                                        BigDecimal price, List<AnimalAmenity> animalAmenities, OfferRepository offerRepository) {
        Offer offer = createComplexMockOfferForCaretaker(caretaker, animal, animalAttributes, price, animalAmenities);
        return offerRepository.save(offer);
    }

    public static Offer addComplexOffer(Caretaker caretaker, Animal animal, List<AnimalAttribute> animalAttributes,
                                        BigDecimal price, List<AnimalAmenity> animalAmenities, OfferRepository offerRepository,
                                        Offer offer) {
        Offer offerToSave = createComplexMockOfferForCaretaker(caretaker, animal, animalAttributes, price, animalAmenities, offer);
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

    public static Care addCare(CareRepository careRepository, Caretaker caretaker,
                               Client client, Animal animal) {
        Care care = createMockCare(caretaker, client, animal);
        return careRepository.saveAndFlush(care);
    }
}
