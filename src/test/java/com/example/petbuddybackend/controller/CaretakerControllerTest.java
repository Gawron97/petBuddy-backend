package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.address.AddressDTO;
import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import com.example.petbuddybackend.dto.user.*;
import com.example.petbuddybackend.entity.address.Voivodeship;
import com.example.petbuddybackend.entity.user.Role;
import com.example.petbuddybackend.service.user.CaretakerService;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class CaretakerControllerTest {

    private static final String PHONE_NUMBER = "123456789";
    private static final String DESCRIPTION = "Test description";
    private static final String CITY = "City";
    private static final String ZIP_CODE = "00-000";
    private static final String STREET = "Street";
    private static final String STREET_NUMBER = "10";
    private static final String APARTMENT_NUMBER = "20";
    private static final String UPDATED_PHONE_NUMBER = "987654321";
    private static final String UPDATED_DESCRIPTION = "Updated description";
    private static final String UPDATED_CITY = "New City";
    private static final String UPDATED_ZIP_CODE = "11-111";
    private static final String UPDATED_STREET = "New Street";
    private static final String UPDATED_STREET_NUMBER = "11";
    private static final String UPDATED_APARTMENT_NUMBER = "21";
    private static final String CARETAKER_EMAIL = "caretaker_email";
    private static final String CLIENT_EMAIL = "client";
    private static final String RATING_BODY = "{\"rating\": %d, \"comment\": \"%s\"}";

    private static final String CREATE_CARETAKER_BODY = """
            {
                "phoneNumber": "%s",
                "description": "%s",
                "address": {
                    "city": "%s",
                    "zipCode": "%s",
                    "voivodeship": "%s",
                    "street": "%s",
                    "streetNumber": "%s",
                    "apartmentNumber": "%s"
                }
            }
            """;

    private static final String UPDATE_CARETAKER_BODY = """
            {
                "phoneNumber": "%s",
                "description": "%s",
                "address": {
                    "city": "%s",
                    "zipCode": "%s",
                    "voivodeship": "%s",
                    "street": "%s",
                    "streetNumber": "%s",
                    "apartmentNumber": "%s"
                },
                "offerBlobsToKeep": []
            }
            """;

    private static final Gson gson = new Gson();

    @Value("${header-name.role}")
    private String ROLE_HEADER_NAME;

    @MockBean
    private CaretakerService caretakerService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCaretakers_shouldReturnFilteredResults() throws Exception {
        CaretakerDTO caretakerComplexInfoDTO1 = CaretakerDTO.builder()
                .accountData(AccountDataDTO.builder().name("John Doe").build())
                .build();

        CaretakerDTO caretakerComplexInfoDTO2 = CaretakerDTO.builder()
                .accountData(AccountDataDTO.builder().name("Jane Doe").build())
                .build();

        List<CaretakerDTO> caretakerComplexInfoDTOS = List.of(caretakerComplexInfoDTO1, caretakerComplexInfoDTO2);
        Page<CaretakerDTO> page = new PageImpl<>(
                caretakerComplexInfoDTOS,
                PageRequest.of(0, 10), caretakerComplexInfoDTOS.size()
        );

        when(caretakerService.getCaretakers(any(), any(), any())).thenReturn(page);

        mockMvc.perform(post("/api/caretaker")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "name,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].accountData.name").value("John Doe"))
                .andExpect(jsonPath("$.content[1].accountData.name").value("Jane Doe"));
    }

    @ParameterizedTest
    @MethodSource("provideRatingData")
    @WithMockUser(username = CLIENT_EMAIL)
    void rateCaretaker(String body, ResultMatcher expectedResponse) throws Exception {
        mockMvc.perform(post("/api/caretaker/1/rating")
                        .header(ROLE_HEADER_NAME, Role.CLIENT)
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(expectedResponse);
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL)
    void deleteRating_shouldSucceed() throws Exception {
        mockMvc.perform(delete("/api/caretaker/1/rating")
                        .header(ROLE_HEADER_NAME, Role.CLIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getRatingsOfCaretaker_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/caretaker/1/rating")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private static Stream<Arguments> provideRatingData() {
        return Stream.of(
                Arguments.of(String.format(RATING_BODY, 5, "Great service!"), status().isOk()),
                Arguments.of(String.format(RATING_BODY, 3, ""), status().isOk()),
                Arguments.of(String.format(RATING_BODY, 1, ""), status().isOk()),
                Arguments.of(String.format(RATING_BODY, 6, "Great service!"), status().isBadRequest()),
                Arguments.of(String.format(RATING_BODY, 0, "Great service!"), status().isBadRequest()),
                Arguments.of("{\"comment\": \"comment\"}", status().isBadRequest()),
                Arguments.of("{\"rating\": 4}", status().isBadRequest())
        );
    }

    @Test
    void getCaretaker_shouldReturnCaretakerDetails() throws Exception {
        // Given
        String caretakerEmail = "johndoe@example.com";
        CaretakerComplexInfoDTO caretakerComplexInfoDTO = CaretakerComplexInfoDTO.builder()
                .accountData(AccountDataDTO.builder()
                        .email(caretakerEmail)
                        .name("John Doe")
                        .build())
                .description("Experienced pet caretaker")
                .build();

        when(caretakerService.getCaretaker(caretakerEmail)).thenReturn(caretakerComplexInfoDTO);

        // When Then
        mockMvc.perform(get("/api/caretaker/{caretakerEmail}", caretakerEmail)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountData.email").value(caretakerEmail))
                .andExpect(jsonPath("$.accountData.name").value("John Doe"))
                .andExpect(jsonPath("$.description").value("Experienced pet caretaker"));
    }

    @Test
    void getCaretaker_whenCaretakerNotExists_shouldThrowNotFound() throws Exception {
        // Given
        String caretakerEmail = "johndoe@example.com";

        when(caretakerService.getCaretaker(caretakerEmail)).thenThrow(NotFoundException.class);

        // When Then
        mockMvc.perform(get("/api/caretaker/{caretakerEmail}", caretakerEmail)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void addCaretaker_shouldReturnCreatedCaretaker() throws Exception {
        // When
        String json = createRequestBody();
        ModifyCaretakerDTO dto = gson.fromJson(json, ModifyCaretakerDTO.class);
        CaretakerComplexInfoDTO resultDTO = createRequestResponse();

        MockMultipartFile caretakerData = getMockMultipartFile(json);
        MockMultipartFile newOfferPhotos = getMockMultipartPhotoFile();

        // When
        when(caretakerService.addCaretaker(eq(dto), eq(CARETAKER_EMAIL), any()))
                .thenReturn(resultDTO);

        // Then
        mockMvc.perform(multipart(HttpMethod.POST, "/api/caretaker/add")
                        .file(caretakerData)
                        .file(newOfferPhotos))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value(PHONE_NUMBER))
                .andExpect(jsonPath("$.description").value(DESCRIPTION))
                .andExpect(jsonPath("$.address.city").value(CITY))
                .andExpect(jsonPath("$.address.zipCode").value(ZIP_CODE))
                .andExpect(jsonPath("$.address.voivodeship").value(Voivodeship.MAZOWIECKIE.name()))
                .andExpect(jsonPath("$.address.street").value(STREET))
                .andExpect(jsonPath("$.address.streetNumber").value(STREET_NUMBER))
                .andExpect(jsonPath("$.address.apartmentNumber").value(APARTMENT_NUMBER));
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void editCaretaker_shouldUpdateOnlyProvidedFields() throws Exception {
        // Given
        String json = createUpdatedRequestBody();
        ModifyCaretakerDTO dto = gson.fromJson(json, ModifyCaretakerDTO.class);
        CaretakerComplexInfoDTO resultDTO = createUpdatedRequestResponse();

        MockMultipartFile caretakerData = getMockMultipartFile(json);
        MockMultipartFile newOfferPhotos = getMockMultipartPhotoFile();
        MockMultipartFile offerBlobsMultipart = new MockMultipartFile(
                "offerBlobsToKeep",
                "empty-photo.jpg",
                MediaType.APPLICATION_JSON_VALUE,
                "[]".getBytes(StandardCharsets.UTF_8)
        );

        // When
        when(caretakerService.editCaretaker(eq(dto), eq(CARETAKER_EMAIL), any(), any()))
                .thenReturn(resultDTO);

        // Then
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/caretaker/edit")
                        .file(caretakerData)
                        .file(offerBlobsMultipart)
                        .file(newOfferPhotos)
                        .header(ROLE_HEADER_NAME, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value(UPDATED_PHONE_NUMBER))
                .andExpect(jsonPath("$.description").value(UPDATED_DESCRIPTION))
                .andExpect(jsonPath("$.address.city").value(UPDATED_CITY))
                .andExpect(jsonPath("$.address.zipCode").value(UPDATED_ZIP_CODE))
                .andExpect(jsonPath("$.address.voivodeship").value(Voivodeship.PODLASKIE.name()))
                .andExpect(jsonPath("$.address.street").value(UPDATED_STREET))
                .andExpect(jsonPath("$.address.streetNumber").value(UPDATED_STREET_NUMBER))
                .andExpect(jsonPath("$.address.apartmentNumber").value(UPDATED_APARTMENT_NUMBER));
    }

    @Test
    @WithMockUser(username = CARETAKER_EMAIL)
    void editCaretakerOfferPhotos_shouldSucceed() throws Exception {
        String blob1 = "blob1";
        String url1 = "https://example.com/photo1";
        String blob2 = "blob2";
        String url2 = "https://example.com/photo2";

        Set<String> currentOfferBlobs = Collections.emptySet();
        MockMultipartFile newOfferPhotosMultipart = getMockMultipartPhotoFile();

        PhotoLinkDTO firstPhoto = new PhotoLinkDTO(blob1, url1);
        PhotoLinkDTO secondPhoto = new PhotoLinkDTO(blob2, url2);
        List<PhotoLinkDTO> expectedOutput = List.of(firstPhoto, secondPhoto);

        when(caretakerService.putOfferPhotos(eq(CARETAKER_EMAIL), eq(currentOfferBlobs), any()))
                .thenReturn(expectedOutput);


        MockMultipartFile offerBlobsMultipart = new MockMultipartFile(
                "offerBlobsToKeep",
                "empty-photo.jpg",
                MediaType.APPLICATION_JSON_VALUE,
                "[]".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/caretaker/offer-photo")
                        .file(offerBlobsMultipart)
                        .file(newOfferPhotosMultipart)
                        .header(ROLE_HEADER_NAME, Role.CARETAKER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].blob").value(blob1))
                .andExpect(jsonPath("$[0].url").value(url1))
                .andExpect(jsonPath("$[1].blob").value(blob2))
                .andExpect(jsonPath("$[1].url").value(url2));
    }

    private static MockMultipartFile getMockMultipartPhotoFile() {
        return new MockMultipartFile(
                "newOfferPhotos",
                "empty-photo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );
    }

    private static MockMultipartFile getMockMultipartFile(String json) {
        return new MockMultipartFile(
                "caretakerData",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                json.getBytes()
        );
    }

    private static String createRequestBody() {
        return String.format(
                CREATE_CARETAKER_BODY,
                PHONE_NUMBER,
                DESCRIPTION,
                CITY,
                ZIP_CODE,
                Voivodeship.MAZOWIECKIE.name(),
                STREET,
                STREET_NUMBER,
                APARTMENT_NUMBER
        );
    }

    private static String createUpdatedRequestBody() {
        return String.format(
                UPDATE_CARETAKER_BODY,
                UPDATED_PHONE_NUMBER,
                UPDATED_DESCRIPTION,
                UPDATED_CITY,
                UPDATED_ZIP_CODE,
                Voivodeship.PODLASKIE.name(),
                UPDATED_STREET,
                UPDATED_STREET_NUMBER,
                UPDATED_APARTMENT_NUMBER
        );
    }

    private static CaretakerComplexInfoDTO createRequestResponse() {
        AccountDataDTO accountDataDTO = AccountDataDTO.builder()
                .email(CARETAKER_EMAIL)
                .surname("caretaker surname")
                .name("caretaker name")
                .build();

        AddressDTO addressDTO = AddressDTO.builder()
                .city(CITY)
                .apartmentNumber(APARTMENT_NUMBER)
                .street(STREET)
                .apartmentNumber(APARTMENT_NUMBER)
                .zipCode(ZIP_CODE)
                .voivodeship(Voivodeship.MAZOWIECKIE)
                .streetNumber(STREET_NUMBER)
                .build();

        return CaretakerComplexInfoDTO.builder()
                .accountData(accountDataDTO)
                .address(addressDTO)
                .description(DESCRIPTION)
                .phoneNumber(PHONE_NUMBER)
                .build();
    }

    private static CaretakerComplexInfoDTO createUpdatedRequestResponse() {
        AccountDataDTO accountDataDTO = AccountDataDTO.builder()
                .email(CARETAKER_EMAIL)
                .surname("caretaker surname")
                .name("caretaker name")
                .build();

        AddressDTO addressDTO = AddressDTO.builder()
                .city(UPDATED_CITY)
                .apartmentNumber(UPDATED_APARTMENT_NUMBER)
                .street(UPDATED_STREET)
                .zipCode(UPDATED_ZIP_CODE)
                .apartmentNumber(UPDATED_APARTMENT_NUMBER)
                .voivodeship(Voivodeship.PODLASKIE)
                .streetNumber(UPDATED_STREET_NUMBER)
                .build();

        return CaretakerComplexInfoDTO.builder()
                .accountData(accountDataDTO)
                .address(addressDTO)
                .description(UPDATED_DESCRIPTION)
                .phoneNumber(UPDATED_PHONE_NUMBER)
                .build();
    }
}
