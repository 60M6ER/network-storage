package com.larionov.storage.core.download.exeptions;

public class ErrorReceiveFile extends RuntimeException{
    public ErrorReceiveFile() {
    }

    public ErrorReceiveFile(String message) {
        super(message);
    }
}
