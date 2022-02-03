package com.larionov.storage.core.files;

import lombok.Getter;

import java.nio.file.Path;

public class FileHandler {

    @Getter
    private String viewDir;
    private Path currentDir;
}
