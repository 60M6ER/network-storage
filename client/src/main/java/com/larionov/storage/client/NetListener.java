package com.larionov.storage.client;

import com.larionov.storage.core.net.AuthorizationTrue;
import com.larionov.storage.core.net.ErrorMessage;
import com.larionov.storage.core.net.FileList;

public interface NetListener {
    void onConnectionActive();
    void onConnectionInactive();
    void onError(ErrorMessage message);
    void onFileList(FileList message);
    void onAuthorizationTrue(AuthorizationTrue message);
}
