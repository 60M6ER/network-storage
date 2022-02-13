package com.larionov.storage.core.net;

import lombok.Getter;

public class DeleteFile extends AbstractMessage {

    @Getter
    private String nameFile;

    public DeleteFile(String nameFile) {
        this.nameFile = nameFile;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.DELETE;
    }
}
