package com.larionov.storage.core.download;

import com.larionov.storage.core.net.AbstractMessage;

public interface NetHandler {
    void sendMessage(AbstractMessage abstractMessage);
}
