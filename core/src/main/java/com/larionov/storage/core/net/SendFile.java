package com.larionov.storage.core.net;

import lombok.Getter;

public class SendFile extends AbstractMessage {

    @Getter
    private String nameFile;
    @Getter
    private long size;
    @Getter
    private byte[] data;

    public SendFile(String nameFile, long size, byte[] data) {
        this.nameFile = nameFile;
        this.size = size;
        this.data = data;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.SEND_FILE;
    }
}
