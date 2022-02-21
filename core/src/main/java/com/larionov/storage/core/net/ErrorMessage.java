package com.larionov.storage.core.net;

import lombok.Getter;

public class ErrorMessage extends AbstractMessage {

    @Getter
    private String message;
    @Getter
    private Exception exception;

    public ErrorMessage(String message) {
        this.message = message;
    }

    public ErrorMessage(String message, Exception exception) {
        this.message = message;
        this.exception = exception;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.ERROR;
    }
}
