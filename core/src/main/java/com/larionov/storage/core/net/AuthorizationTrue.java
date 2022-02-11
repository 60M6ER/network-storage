package com.larionov.storage.core.net;

import lombok.Getter;

public class AuthorizationTrue extends Message {

    @Getter
    private String message;

    public AuthorizationTrue(String message) {
        this.message = message;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.AUTH_TRUE;
    }
}
