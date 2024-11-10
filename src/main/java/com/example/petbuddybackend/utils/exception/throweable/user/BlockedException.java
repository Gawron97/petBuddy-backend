package com.example.petbuddybackend.utils.exception.throweable.user;

import com.example.petbuddybackend.utils.exception.throweable.HttpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class BlockedException extends HttpException {

    private static final HttpStatusCode HTTP_STATUS = HttpStatus.FORBIDDEN;
    private static final String USER_BLOCKED_EXCEPTION = "User %s has been blocked by %s";
    private static final String USERS_BLOCKED_EACH_OTHER_MESSAGE = "Users have blocked each other";

    public BlockedException(String blockerUsername, String blockedUsername) {
        super(String.format(USER_BLOCKED_EXCEPTION, blockedUsername, blockerUsername), HTTP_STATUS);
    }

    public BlockedException() {
        super(USERS_BLOCKED_EACH_OTHER_MESSAGE, HTTP_STATUS);
    }
}
