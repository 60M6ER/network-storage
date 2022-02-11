package com.larionov.storage.core.net;

import lombok.Getter;

public class AuthMessage extends Message {

    @Getter
    private String Login;
    @Getter
    private String Password;

    public AuthMessage(String login, String password) {
        Login = login;
        Password = password;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.AUTH;
    }
}
