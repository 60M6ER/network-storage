package com.larionov.storage.core.net;

public class QueryFileList extends AbstractMessage {

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.QUERY_FILE_LIST;
    }
}
