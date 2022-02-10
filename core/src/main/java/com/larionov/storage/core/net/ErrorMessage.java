package com.larionov.storage.core.net;

import lombok.Getter;

public class ErrorMessage implements Message{

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