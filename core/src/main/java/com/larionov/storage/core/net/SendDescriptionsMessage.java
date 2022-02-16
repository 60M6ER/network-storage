package com.larionov.storage.core.net;

import lombok.Getter;

import java.util.UUID;

public class SendDescriptionsMessage extends AbstractMessage {

    @Getter
    private UUID idTransfer;
    @Getter
    private long size;

    public SendDescriptionsMessage(UUID idTransfer, long size) {
        this.idTransfer = idTransfer;
        this.size = size;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.SEND_DESCRIPTIONS;
    }
}
