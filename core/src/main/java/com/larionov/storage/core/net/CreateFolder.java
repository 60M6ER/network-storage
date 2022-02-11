package com.larionov.storage.core.net;

import lombok.Getter;

public class CreateFolder extends Message {

    @Getter
    private String nameFile;

    public CreateFolder(String nameFile) {
        this.nameFile = nameFile;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.CREATE_FOLDER;
    }
}
