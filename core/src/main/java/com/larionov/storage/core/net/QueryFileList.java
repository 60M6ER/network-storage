package com.larionov.storage.core.net;

import lombok.Getter;

import java.nio.file.Path;

public class QueryFileList extends AbstractMessage {

    @Getter
    private Path path;

    public QueryFileList(Path path) {
        this.path = path;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.QUERY_FILE_LIST;
    }
}
