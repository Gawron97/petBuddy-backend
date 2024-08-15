package com.example.petbuddybackend.testutils;

import com.example.petbuddybackend.entity.amenity.AnimalAmenity;
import com.example.petbuddybackend.entity.animal.Animal;
import com.example.petbuddybackend.entity.animal.AnimalAttribute;
import com.example.petbuddybackend.entity.offer.Offer;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.animal.AnimalRepository;
import com.example.petbuddybackend.repository.offer.OfferRepository;
import com.example.petbuddybackend.repository.user.AppUserRepository;
import com.example.petbuddybackend.repository.user.CaretakerRepository;
import com.example.petbuddybackend.repository.user.ClientRepository;
import org.mockito.Mock;

import javax.swing.text.Caret;
import java.util.List;

import static com.example.petbuddybackend.testutils.MockUtils.*;

public class PersistenceUtils {

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

        offerRepository.saveAllAndFlush(MockUtils.createMockOffers(caretakers, animals));

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
                                        List<AnimalAmenity> animalAmenities, OfferRepository offerRepository) {
        Offer offer = MockUtils.createComplexMockOfferForCaretaker(caretaker, animal, animalAttributes, animalAmenities);
        return offerRepository.save(offer);
    }

}
