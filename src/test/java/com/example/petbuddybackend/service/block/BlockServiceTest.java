package com.example.petbuddybackend.service.block;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.entity.block.Block;
import com.example.petbuddybackend.entity.block.BlockId;
import com.example.petbuddybackend.entity.user.AppUser;
import com.example.petbuddybackend.repository.block.BlockRepository;
import com.example.petbuddybackend.service.user.UserService;
import com.example.petbuddybackend.testutils.mock.MockUserProvider;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.user.AlreadyBlockedException;
import com.example.petbuddybackend.utils.exception.throweable.user.BlockedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
public class BlockServiceTest {

    @Autowired
    private BlockService blockService;

    @MockBean
    private BlockRepository blockRepository;

    @MockBean
    private UserService userService;

    @Test
    void getUsersBlockedByUserSortedByBlockedUsername_shouldReturnSortedAccountDataDTOs() {
        // Given
        String blockerUsername = "blocker@example.com";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "blockedEmail"));

        AppUser blockedUser1 = MockUserProvider
                .createMockAppUser("name1", "surname1", "blocked1@example.com");

        AppUser blockedUser2 = MockUserProvider
                .createMockAppUser("name2", "surname2", "blocked2@example.com");

        Block block1 = mock(Block.class);
        Block block2 = mock(Block.class);

        when(block1.getBlocked()).thenReturn(blockedUser1);
        when(block2.getBlocked()).thenReturn(blockedUser2);

        List<Block> blocks = List.of(block1, block2);

        when(blockRepository.findByBlockerEmail(any(String.class))).thenReturn(blocks);
        when(userService.renewProfilePicture(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<AccountDataDTO> result = blockService.getUsersBlockedByUserSortedByBlockedUsername(blockerUsername);

        // Then
        assertEquals(2, result.size());
        assertEquals(blockedUser1.getEmail(), result.get(0).email());
        assertEquals(blockedUser2.getEmail(), result.get(1).email());
    }

    @Test
    void blockedEmailPropertyName_shouldMatchBlockProperty() {
        try {
            Field field = Block.class.getDeclaredField("blockedEmail");
            assertEquals(String.class, field.getType(), "The property should be of type String");
        } catch (NoSuchFieldException e) {
            fail("Field not found: " + "blockedEmail");
        }
    }

    @Test
    void blockUser_whenUserBlocksAnotherUser_shouldSucceed() {
        // Given
        String blockerUsername = "blocker@example.com";
        String blockedUsername = "blocked@example.com";

        when(blockRepository.existsById(any(BlockId.class))).thenReturn(false); // User not already blocked

        // When
        blockService.blockUser(blockerUsername, blockedUsername);

        // Then
        verify(blockRepository).save(any(Block.class));
    }

    @Test
    void blockUser_whenUserBlocksThemselves_shouldThrowException() {
        // Given
        String username = "same@example.com";

        // When & Then
        IllegalActionException exception = assertThrows(IllegalActionException.class, () ->
                blockService.blockUser(username, username));
        assertEquals("User cannot block himself", exception.getMessage());
    }

    @Test
    void blockUser_whenUserIsAlreadyBlocked_shouldThrowException() {
        // Given
        String blockerUsername = "blocker@example.com";
        String blockedUsername = "blocked@example.com";

        when(blockRepository.existsById(any(BlockId.class))).thenReturn(true); // User already blocked

        // When & Then
        assertThrows(AlreadyBlockedException.class, () ->
                blockService.blockUser(blockerUsername, blockedUsername));
    }

    @Test
    void unblockUser_whenBlockExists_shouldSucceed() {
        // Given
        String blockerUsername = "blocker@example.com";
        String blockedUsername = "blocked@example.com";

        when(blockRepository.findById(any(BlockId.class))).thenReturn(Optional.of(new Block(blockerUsername, blockedUsername)));

        // When
        blockService.unblockUser(blockerUsername, blockedUsername);

        // Then
        verify(blockRepository).delete(any(Block.class));
    }

    @Test
    void unblockUser_whenBlockDoesNotExist_shouldThrowException() {
        // Given
        String blockerUsername = "blocker@example.com";
        String blockedUsername = "blocked@example.com";

        when(blockRepository.findById(any(BlockId.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () ->
                blockService.unblockUser(blockerUsername, blockedUsername));
    }

    @Test
    void unblockUser_whenUserUnblocksThemselves_shouldThrowException() {
        // Given
        String username = "same@example.com";

        // When & Then
        assertThrows(IllegalActionException.class, () ->
                blockService.unblockUser(username, username));
    }

    @Test
    void isBlocked_whenUserIsBlocked_shouldReturnTrue() {
        // Given
        String blockerUsername = "blocker@example.com";
        String blockedUsername = "blocked@example.com";

        when(blockRepository.existsById(any(BlockId.class))).thenReturn(true);

        // When
        boolean result = blockService.isBlocked(blockerUsername, blockedUsername);

        // Then
        assertTrue(result);
    }

    @Test
    void isBlocked_whenUserIsNotBlocked_shouldReturnFalse() {
        // Given
        String blockerUsername = "blocker@example.com";
        String blockedUsername = "blocked@example.com";

        when(blockRepository.existsById(any(BlockId.class))).thenReturn(false);

        // When
        boolean result = blockService.isBlocked(blockerUsername, blockedUsername);

        // Then
        assertFalse(result);
    }

    @Test
    void assertNotBlockedByAny_whenBlockedByOtherUser_shouldThrowException() {
        // Given
        String firstUsername = "user1@example.com";
        String secondUsername = "user2@example.com";

        when(blockRepository.existsById(eq(new BlockId(firstUsername, secondUsername)))).thenReturn(true);

        // When & Then
        assertThrows(BlockedException.class, () ->
                blockService.assertNotBlockedByAny(firstUsername, secondUsername));
    }

    @Test
    void assertNotBlockedByAny_whenBlockedByOtherUserReversed_shouldThrowException() {
        // Given
        String firstUsername = "user1@example.com";
        String secondUsername = "user2@example.com";

        when(blockRepository.existsById(eq(new BlockId(secondUsername, firstUsername)))).thenReturn(true);

        // When & Then
        assertThrows(BlockedException.class, () ->
                blockService.assertNotBlockedByAny(firstUsername, secondUsername));
    }
}
