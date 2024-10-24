package com.example.petbuddybackend.utils.exception.throweable.user;

public class BlockedException extends RuntimeException {

    private static final String USER_BLOCKED_EXCEPTION = "User %s has been blocked by %s";

    public BlockedException(String blockerUsername, String blockedUsername) {
        super(String.format(USER_BLOCKED_EXCEPTION, blockedUsername, blockerUsername));
    }
}
