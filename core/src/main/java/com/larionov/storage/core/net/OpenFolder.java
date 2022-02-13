package com.larionov.storage.core.net;

import lombok.Getter;

public class OpenFolder extends AbstractMessage {

    @Getter
    private String nameFile;


    public OpenFolder(String nameFile) {
        this.nameFile = nameFile;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.OPEN_FOLDER;
    }
}
