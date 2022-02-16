package com.larionov.storage.core.net;

import lombok.Getter;

import java.nio.file.Path;

public class SendFile extends AbstractMessage {

    @Getter
    private String pathFile;
    @Getter
    private long size;
    @Getter
    private byte[] data;

    public SendFile(String pathFile, long size, byte[] data) {
        this.pathFile = pathFile;
        this.size = size;
        this.data = data;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.SEND_FILE;
    }
}
