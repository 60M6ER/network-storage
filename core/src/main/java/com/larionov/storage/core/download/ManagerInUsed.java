package com.larionov.storage.core.download;

public class ManagerInUsed extends RuntimeException{
    public ManagerInUsed(String message) {
        super(message);
    }
}
