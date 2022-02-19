package com.larionov.storage.core.download;

import com.larionov.storage.core.net.AbstractMessage;

public interface TransferListener {
    void startProcess(TransferService sendManager);
    void startSendFiles(TransferService sendManager);
    void finishedDownload(TransferService sendManager);
    void anExceptionOccurred(Exception e, TransferService fileSendManager);
    void newSendMessage(AbstractMessage message);
}
