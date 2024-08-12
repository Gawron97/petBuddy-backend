package com.example.petbuddybackend.testutils;

import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.entity.user.Client;
import com.example.petbuddybackend.repository.AppUserRepository;
import com.example.petbuddybackend.repository.CaretakerRepository;
import com.example.petbuddybackend.repository.ClientRepository;

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
}
