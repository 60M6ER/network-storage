package com.larionov.storage.core.net;

import lombok.Getter;

public class QueryFile extends AbstractMessage {

    @Getter
    private String nameFile;

    public QueryFile(String nameFile) {
        this.nameFile = nameFile;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.QUERY_FILE;
    }
}
