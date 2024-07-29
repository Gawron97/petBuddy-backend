package com.example.petbuddybackend.service.user;

import com.example.petbuddybackend.dto.address.AddressDTO;
import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.CaretakerDTO;
import com.example.petbuddybackend.dto.user.CaretakerSearchCriteria;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.entity.user.Caretaker;
import com.example.petbuddybackend.repository.AppUserRepository;
import com.example.petbuddybackend.repository.CaretakerRepository;
import com.example.petbuddybackend.testutils.ReflectionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.example.petbuddybackend.entity.animal.AnimalType.CAT;
import static com.example.petbuddybackend.entity.animal.AnimalType.DOG;
import static com.example.petbuddybackend.testutils.MockUtils.createMockCaretakers;
import static com.example.petbuddybackend.testutils.ReflectionUtils.getPrimitiveNames;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class CaretakerServiceTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private CaretakerRepository caretakerRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CaretakerService caretakerService;


    @BeforeEach
    void init() {
        List<Caretaker> caretakers = createMockCaretakers();

        List<AppUser> accountData = caretakers.stream()
                .map(Caretaker::getAccountData)
                .toList();

        appUserRepository.saveAllAndFlush(accountData);
        caretakerRepository.saveAllAndFlush(caretakers);
    }

    @AfterEach
    void cleanUp() {
        appUserRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("provideSpecificationParams")
    void getCaretakersWithFiltering_shouldReturnFilteredResults(
            CaretakerSearchCriteria filters,
            int expectedSize
    ) {
        Page<CaretakerDTO> resultPage = caretakerService.getCaretakers(Pageable.ofSize(10), filters);

        assertEquals(expectedSize, resultPage.getContent().size());
    }

    @Test
    void testSortingParamsAlignWithDTO() {
        List<String> fieldNames = ReflectionUtils.getPrimitiveNames(CaretakerDTO.class);
        fieldNames.addAll(getPrimitiveNames(AddressDTO.class, "address_"));
        fieldNames.addAll(getPrimitiveNames(AccountDataDTO.class, "accountData_"));

        for (String fieldName : fieldNames) {
            Page<CaretakerDTO> resultPage = caretakerService.getCaretakers(
                    PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, fieldName)),
                    CaretakerSearchCriteria.builder().build()
            );

            assertNotNull(resultPage);
            assertFalse(resultPage.getContent().isEmpty());
        }
    }

    private static Stream<Arguments> provideSpecificationParams() {
        return Stream.of(
                Arguments.of(CaretakerSearchCriteria.builder().animalTypes(Set.of(DOG)).build(), 2),
                Arguments.of(CaretakerSearchCriteria.builder().animalTypes(Set.of(CAT)).build(), 1),
                Arguments.of(CaretakerSearchCriteria.builder().animalTypes(Set.of(DOG, CAT)).build(), 2),
                Arguments.of(CaretakerSearchCriteria.builder().personalDataLike("doe").build(), 2),
                Arguments.of(CaretakerSearchCriteria.builder().personalDataLike("testmail").build(), 1),
                Arguments.of(CaretakerSearchCriteria.builder().personalDataLike("john   doe").build(), 1),
                Arguments.of(CaretakerSearchCriteria.builder().personalDataLike("doe  john   ").build(), 1),
                Arguments.of(CaretakerSearchCriteria.builder().personalDataLike(" ").build(), 3),
                Arguments.of(CaretakerSearchCriteria.builder().build(), 3),
                Arguments.of(CaretakerSearchCriteria.builder().voivodeship(Voivodeship.SLASKIE).build(), 1),
                Arguments.of(CaretakerSearchCriteria.builder().cityLike("war").build(), 2)
        );
    }
}
