package com.larionov.storage.core.net;

import java.io.Serializable;

public abstract class AbstractMessage implements Serializable {
    public abstract TypeMessage getTypeMessage();
}
