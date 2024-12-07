package com.example.petbuddybackend.service.block.event;

import com.example.petbuddybackend.service.block.BlockType;

public record BlockEvent(
        String blockerUsername,
        String blockedUsername,
        BlockType blockType
) {
}
