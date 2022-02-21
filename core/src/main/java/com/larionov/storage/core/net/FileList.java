package com.larionov.storage.core.net;

import com.larionov.storage.core.files.FileDescription;
import lombok.Getter;

import java.util.List;

public class FileList extends AbstractMessage {

    @Getter
    private String viewPath;
    @Getter
    private List<FileDescription> list;

    public FileList(String viewPath, List<FileDescription> list) {
        this.viewPath = viewPath;
        this.list = list;
    }

    @Override
    public TypeMessage getTypeMessage() {
        return TypeMessage.FILE_LIST;
    }
}
