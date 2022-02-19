package com.larionov.storage.core.download;

import com.larionov.storage.core.net.AbstractMessage;

import java.nio.file.Path;

public interface TransferService {

    void send();
    String getCurFileName();
    StatusSend getStatus();
    Path getPathFile();
    boolean isActive();
    void stop();
    void sendMessage(AbstractMessage message);
}
