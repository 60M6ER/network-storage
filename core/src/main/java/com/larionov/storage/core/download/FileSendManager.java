package com.larionov.storage.core.download;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

@Slf4j
public class FileSendManager {
    private static final int BUFF_SIZE = 2048;
    private static final long SIZE_FOLDER = 20;
    @Getter
    private Thread thread;
    private boolean failure = false;

    private Path pathFile;
    private long size = 0;
    private long sentBytes = 0;
    @Setter
    private NetHandler netHandler;
    @Setter
    private StatusSenderListener statusSenderListener;

    public FileSendManager(Path pathFile, NetHandler netHandler, StatusSenderListener statusSenderListener) {
        this.pathFile = pathFile;
        this.netHandler = netHandler;
        this.statusSenderListener = statusSenderListener;
    }

    public FileSendManager(Path pathFile, NetHandler netHandler) {
        this.pathFile = pathFile;
        this.netHandler = netHandler;
    }

    public void send() {
        if (thread == null) {
            thread = new Thread(this::start);
            thread.setDaemon(true);
            thread.start();
        }else {
            throw new ManagerInUsed("This manager already in used. Please create new manager");
        }
    }

    private void start(){
        if (statusSenderListener != null) statusSenderListener.startProcess(this);
        try {
            calculateSend(pathFile);
            if (statusSenderListener != null) statusSenderListener.startSendFiles(this);

        } catch (Exception e) {
            if (statusSenderListener != null) statusSenderListener.anExceptionOccurred(e, this);
        }
    }

    private void sendFiles(Path path) {
        try {
            if (Files.isDirectory(path)) {
                sentBytes += SIZE_FOLDER;
                // TODO: send message on server
                Files.list(path).forEach(this::sendFiles);
            } else {
                long sizeCurFile = Files.size(path);
                if (sizeCurFile <= BUFF_SIZE) {
                    // TODO: send message on server
                } else {
                    Files.newInputStream(path, OpenOption)
                }
            }
        } catch (IOException e) {
            failure = true;
            if (statusSenderListener != null) statusSenderListener.anExceptionOccurred(e, this);
        }
    }

    private void calculateSend(Path path) {
        try {
            if (Files.isDirectory(path)) {
                size += SIZE_FOLDER;
                Files.list(path).forEach(this::calculateSend);
            } else {
                size += Files.size(path);
            }
        } catch (IOException e) {
            failure = true;
            if (statusSenderListener != null) statusSenderListener.anExceptionOccurred(e, this);
        }
    }
}
