package com.larionov.storage.core.net;

import lombok.Getter;

import java.io.File;
import java.util.List;

public class FileList implements Message{

    @Getter
    private String viewPath;
    @Getter
    private List<String> list;

    public FileList(String viewPath, List<String> list) {
        this.viewPath = viewPath;
        this.list = list;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.FILE_LIST;
    }
}
