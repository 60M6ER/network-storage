package com.larionov.storage.server;

import com.larionov.storage.core.auth.AuthorizationService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SimpleAuthorizationService implements AuthorizationService {

    private static final String NAME_FILE = "users";

    private Path fileUsers;

    public SimpleAuthorizationService(Path dir) throws IOException {
        this.fileUsers = dir.resolve(NAME_FILE);
        if (!Files.exists(fileUsers))
            Files.createFile(fileUsers);
    }

    @Override
    public boolean authorize(String login, String password) throws IOException {
        AtomicBoolean created = new AtomicBoolean(false);
        AtomicBoolean authorized = new AtomicBoolean(false);
        try {
            Files.lines(fileUsers).
                    map(str -> {
                        String[] split = str.split("|");
                        return new pairAuth(split[0], split[1]);
                    }).
                    filter(pairAuth -> pairAuth.login.equals(login)).
                    forEach(pairAuth -> {
                        created.set(true);
                        if (pairAuth.password.equals(password))
                            authorized.set(true);
                    });
        } catch (IOException e) {
            log.info("File users is empty. A new user will be created.");
        }
        if (!created.get()) {
            String pair = login + "|" + password + "\n";
            Files.write(fileUsers, pair.getBytes());
            authorized.set(true);
        }
        return authorized.get();
    }

    private class pairAuth{
        public String login;
        public String password;

        public pairAuth(String login, String password) {
            this.login = login;
            this.password = password;
        }
    }
}
