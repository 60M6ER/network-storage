package com.larionov.storage.core.net;

import lombok.Getter;

public class ErrorMessage extends AbstractMessage {

    @Getter
    private String message;

    public ErrorMessage(String message) {
        this.message = message;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.ERROR;
    }
}
