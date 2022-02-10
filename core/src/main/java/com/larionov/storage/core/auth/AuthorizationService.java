package com.larionov.storage.core.auth;

import java.io.IOException;

public interface AuthorizationService {

    boolean authorize(String login, String password) throws IOException;
}
