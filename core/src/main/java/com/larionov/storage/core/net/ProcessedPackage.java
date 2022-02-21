package com.larionov.storage.core.net;

import lombok.Getter;

public class ProcessedPackage extends AbstractMessage {
    @Getter
    boolean OK;

    public ProcessedPackage(boolean OK) {
        this.OK = OK;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.PROCESSED_PACKAGE;
    }
}
