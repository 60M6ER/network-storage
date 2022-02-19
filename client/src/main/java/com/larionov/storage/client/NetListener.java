package com.larionov.storage.client;

import com.larionov.storage.core.net.*;

public interface NetListener {
    void onConnectionActive();
    void onConnectionInactive();
    void onError(ErrorMessage message);
    void onFileList(FileList message);
    void onAuthorizationTrue(AuthorizationTrue message);
    void onProcessedPackage(ProcessedPackage message);
    void onTransferCommands(AbstractMessage message);
}
