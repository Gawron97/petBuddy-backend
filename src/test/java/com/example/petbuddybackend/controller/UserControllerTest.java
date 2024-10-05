package com.example.petbuddybackend.controller;

import com.example.petbuddybackend.dto.photo.PhotoLinkDTO;
import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.dto.user.UserProfilesData;
import com.example.petbuddybackend.service.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    public static final String USERNAME = "testuser";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    @WithMockUser(USERNAME)
    void getUserProfiles_shouldReturnUserProfiles() throws Exception {
        // given
        String name = "user name";
        String surname = "user surname";
        AccountDataDTO accountData = new AccountDataDTO(USERNAME, name, surname, null);
        UserProfilesData profileData = new UserProfilesData(accountData, true, false);

        // when
        when(userService.getProfileData(USERNAME)).thenReturn(profileData);

        // then
        mockMvc.perform(get("/api/user/available-profiles")
                        .with(user(USERNAME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountData.email").value(USERNAME))
                .andExpect(jsonPath("$.accountData.name").value(name))
                .andExpect(jsonPath("$.accountData.surname").value(surname))
                .andExpect(jsonPath("$.hasClientProfile").value(true))
                .andExpect(jsonPath("$.hasCaretakerProfile").value(false));
    }

    @Test
    @WithMockUser(USERNAME)
    void uploadProfilePicture_shouldReturnPhotoLinkDTO() throws Exception {
        // given
        String url = "http://example.com/profile.jpg";
        String blob = "someBlob";
        MockMultipartFile mockFile = new MockMultipartFile("profilePicture", "profile.jpg", "image/jpeg", "test image".getBytes());

        PhotoLinkDTO photoLinkDTO = new PhotoLinkDTO(blob, url);
        AccountDataDTO accountDataDTO = new AccountDataDTO(USERNAME, "name", "surname", photoLinkDTO);

        // when
        when(userService.uploadProfilePicture(eq(USERNAME), any(MultipartFile.class))).thenReturn(accountDataDTO);

        // then
        mockMvc.perform(multipart("/api/user/profile-picture")
                        .file(mockFile)
                        .with(user(USERNAME)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USERNAME))
                .andExpect(jsonPath("$.profilePicture.url").value(url))
                .andExpect(jsonPath("$.profilePicture.blob").value(blob));
    }

    @Test
    @WithMockUser(USERNAME)
    void uploadProfilePicture_noPictureProvided_shouldReturn400() throws Exception {
        // then
        mockMvc.perform(multipart("/api/user/profile-picture")
                        .with(user(USERNAME)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(USERNAME)
    void deleteProfilePicture_shouldReturnNoContent() throws Exception {
        // when & then
        doNothing().when(userService).deleteProfilePicture(USERNAME);

        mockMvc.perform(delete("/api/user/profile-picture")
                        .with(user(USERNAME)))
                .andExpect(status().isOk());
    }
}
