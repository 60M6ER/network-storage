package com.larionov.storage.core.download;

import lombok.Getter;
import lombok.Setter;

public class StatusSend {

    @Getter
    @Setter
    private long timePassed;
    @Getter
    @Setter
    private long timeLeft;
    @Getter
    @Setter
    private double globalProgress;
    @Getter
    @Setter
    private String messageStatus;

    public StatusSend() {
    }
}
