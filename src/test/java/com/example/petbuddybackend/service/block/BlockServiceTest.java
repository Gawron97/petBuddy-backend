package com.example.petbuddybackend.service.block;

import com.example.petbuddybackend.entity.block.Block;
import com.example.petbuddybackend.entity.block.BlockId;
import com.example.petbuddybackend.repository.block.BlockRepository;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.user.AlreadyBlockedException;
import com.example.petbuddybackend.utils.exception.throweable.user.BlockedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class BlockServiceTest {

    @Autowired
    private BlockService blockService;

    @MockBean
    private BlockRepository blockRepository;

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
