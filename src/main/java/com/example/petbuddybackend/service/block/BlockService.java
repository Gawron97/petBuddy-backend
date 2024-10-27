package com.example.petbuddybackend.service.block;

import com.example.petbuddybackend.dto.user.AccountDataDTO;
import com.example.petbuddybackend.entity.block.Block;
import com.example.petbuddybackend.entity.block.BlockId;
import com.example.petbuddybackend.repository.block.BlockRepository;
import com.example.petbuddybackend.service.care.state.CareStateMachine;
import com.example.petbuddybackend.service.mapper.UserMapper;
import com.example.petbuddybackend.service.user.UserService;
import com.example.petbuddybackend.utils.exception.throweable.general.IllegalActionException;
import com.example.petbuddybackend.utils.exception.throweable.general.NotFoundException;
import com.example.petbuddybackend.utils.exception.throweable.user.AlreadyBlockedException;
import com.example.petbuddybackend.utils.exception.throweable.user.BlockedException;
import com.example.petbuddybackend.utils.paging.PagingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlockService {

    private static final String USER_CANNOT_BLOCK_HIMSELF_MESSAGE = "User cannot block himself";
    private static final String USER_ALREADY_BLOCKED_MESSAGE = "User %s is already blocked";
    private static final String BLOCK = "Block";
    private static final String BLOCKED_EMAIL_PROPERTY_NAME = "blockedEmail";

    private final BlockRepository blockRepository;
    private final CareStateMachine careStateMachine;
    private final UserService userService;
    private final UserMapper userMapper = UserMapper.INSTANCE;

    public Page<AccountDataDTO> getUsersBlockedByUserSortedByBlockedUsername(String username, Pageable pageable) {
        Pageable sortedPageable = PagingUtils.sortedBy(pageable, BLOCKED_EMAIL_PROPERTY_NAME);

        return blockRepository.findByBlockerEmail(username, sortedPageable)
                .map(Block::getBlocked)
                .map(userService::renewProfilePicture)
                .map(userMapper::mapToAccountDataDTO);
    }

    public Block getBlock(String blockerUsername, String blockedUsername) {
        return blockRepository.findById(new BlockId(blockerUsername, blockedUsername))
                .orElseThrow(() -> NotFoundException.withFormattedMessage(BLOCK, blockedUsername));
    }

    public void blockUser(String blockerUsername, String blockedUsername) {
        assertDoesNotBlockSelf(blockerUsername, blockedUsername);
        assertNotAlreadyBlocked(blockerUsername, blockedUsername);

        blockRepository.save(new Block(blockerUsername, blockedUsername));
        careStateMachine.cancelCaresIfStatePermitsAndSave(blockerUsername, blockedUsername);
    }

    public void unblockUser(String blockerUsername, String blockedUsername) {
        assertDoesNotBlockSelf(blockerUsername, blockedUsername);

        Block block = getBlock(blockerUsername, blockedUsername);
        blockRepository.delete(block);
    }

    public boolean isBlocked(String blockerUsername, String blockedUsername) {
        return blockRepository.existsById(new BlockId(blockerUsername, blockedUsername));
    }

    public void assertNotBlockedByAny(String firstUsername, String secondUsername) {
        assertNotBlocked(firstUsername, secondUsername);
        assertNotBlocked(secondUsername, firstUsername);
    }

    private void assertNotBlocked(String firstUsername, String secondUsername) {
        if(isBlocked(firstUsername, secondUsername)) {
            throw new BlockedException(firstUsername, secondUsername);
        }
    }

    private void assertNotAlreadyBlocked(String blockerUsername, String blockedUsername) {
        if(isBlocked(blockerUsername, blockedUsername)) {
            throw new AlreadyBlockedException(USER_ALREADY_BLOCKED_MESSAGE);
        }
    }

    private void assertDoesNotBlockSelf(String blockerUsername, String blockedUsername) {
        if(blockerUsername.equals(blockedUsername)) {
            throw new IllegalActionException(USER_CANNOT_BLOCK_HIMSELF_MESSAGE);
        }
    }
}
