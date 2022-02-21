package com.larionov.storage.core.download.exeptions;

public class ManagerInUsed extends RuntimeException{
    public ManagerInUsed() {
        super("This manager already in used. Please create new manager");
    }
}
