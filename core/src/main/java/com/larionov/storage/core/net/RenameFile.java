package com.larionov.storage.core.net;

import lombok.Getter;

public class RenameFile extends Message {

    @Getter
    private String oldName;
    @Getter
    private String newName;

    public RenameFile(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.RENAME;
    }
}
