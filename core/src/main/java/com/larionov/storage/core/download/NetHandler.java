package com.larionov.storage.core.download;

import com.larionov.storage.core.net.Message;

public interface NetHandler {
    void sendMessage(Message message);
}
