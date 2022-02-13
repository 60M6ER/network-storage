package com.larionov.storage.core.net;

import lombok.Getter;

public class CreateFolder extends AbstractMessage {

    @Getter
    private boolean sendingFile;

    @Getter
    private String nameFile;

    public CreateFolder(String nameFile) {
        this.nameFile = nameFile;
        sendingFile = false;
    }

    public CreateFolder(boolean sendingFile, String nameFile) {
        this.sendingFile = sendingFile;
        this.nameFile = nameFile;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.CREATE_FOLDER;
    }
}
