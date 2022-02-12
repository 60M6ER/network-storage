package com.larionov.storage.core.download;

public interface StatusSenderListener {
    void startProcess(FileSendManager sendManager);
    void startSendFiles(FileSendManager sendManager);
    void sendStatus(FileSendManager sendManager);
    void finishedDownload(FileSendManager sendManager);
    void anExceptionOccurred(Exception e, FileSendManager fileSendManager);
}
