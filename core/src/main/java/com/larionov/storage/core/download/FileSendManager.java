package com.larionov.storage.core.download;

import com.larionov.storage.core.download.exeptions.ManagerInUsed;
import com.larionov.storage.core.net.CreateFolder;
import com.larionov.storage.core.net.SendFile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileSendManager {
    private static final int BUFF_SIZE = 2048;
    private static final long SIZE_FOLDER = 20;
    private static final long SECONDS_DIVIDER = 1000000000L;
    private static final long MINUTES_DIVIDER = 1000000000L * 60;
    private static final long HOUR_DIVIDER = 1000000000L * 60 * 60;

    @Getter
    private Thread thread;
    private boolean failure = false;

    private Path pathFile;
    private long size = 0;
    private long sentBytes = 0;

    private long timeStart;
    @Getter
    private String curFileName;
    private int fileProgress;

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
            throw new ManagerInUsed();
        }
    }

    public StatusSend getStatus(){

        long curTime = System.nanoTime();
        StatusSend statusSend = new StatusSend();
        statusSend.setTimePassed(curTime - timeStart);
        statusSend.setTimeLeft((size - sentBytes) * statusSend.getTimePassed() / sentBytes);
        statusSend.setGlobalProgress(sentBytes * 100.0 / size);

        statusSend.setMessageStatus(String.format(
                "%s -> %s File: %s %s%",
                timesToString(statusSend.getTimePassed()),
                timesToString(statusSend.getTimeLeft()),
                curFileName,
                fileProgress
        ));
        return statusSend;
    }

    private String timesToString(long time){
        int hours = (int) (time / HOUR_DIVIDER);
        long tomeLost = time % HOUR_DIVIDER;
        int minutes = (int) (tomeLost / MINUTES_DIVIDER);
        tomeLost = tomeLost % MINUTES_DIVIDER;
        int seconds = (int) (tomeLost / SECONDS_DIVIDER);

        String result = "";
        result += hours > 0 ? hours + ":" : "";
        result += minutes + ":" + seconds;

        return result;
    }

    private void start(){
        curFileName = pathFile.getFileName().toString();
        if (statusSenderListener != null) statusSenderListener.startProcess(this);
        try {
            calculateSizeToSend(pathFile);
            if (statusSenderListener != null) statusSenderListener.startSendFiles(this);
            timeStart = System.nanoTime();
            sendFiles(pathFile);
            if (statusSenderListener != null) statusSenderListener.finishedDownload(this);
        } catch (Exception e) {
            failure = true;
            if (statusSenderListener != null) statusSenderListener.anExceptionOccurred(e, this);
        }
    }

    private void sendFiles(Path path) {
        try {
            if (Files.isDirectory(path)) {
                sentBytes += SIZE_FOLDER;
                curFileName = path.getFileName().toString();
                fileProgress = 100;
                netHandler.sendMessage(new CreateFolder(
                        true,
                        curFileName
                ));
                if (statusSenderListener != null) statusSenderListener.sendStatus(this);
                Files.list(path).forEach(this::sendFiles);
            } else {
                long sizeCurFile = Files.size(path);
                curFileName = path.getFileName().toString();
                if (sizeCurFile <= BUFF_SIZE) {
                    fileProgress = 100;
                    netHandler.sendMessage(new SendFile(
                            curFileName,
                            Files.size(path),
                            Files.readAllBytes(path)
                    ));
                    sentBytes += sizeCurFile;
                    if (statusSenderListener != null) statusSenderListener.sendStatus(this);
                } else {
                    InputStream inputStream = Files.newInputStream(path);
                    int available = inputStream.available();
                    while (available > 0) {

                        byte[] data = new byte[Math.min(available, BUFF_SIZE)];

                        inputStream.read(data);

                        netHandler.sendMessage(new SendFile(
                                curFileName,
                                sizeCurFile,
                                data
                        ));
                        sentBytes += data.length;
                        fileProgress = (int) (available * 100 / sizeCurFile);
                        if (statusSenderListener != null) statusSenderListener.sendStatus(this);
                        available = inputStream.available();
                    }
                }
            }
        } catch (IOException e) {
            failure = true;
            if (statusSenderListener != null) statusSenderListener.anExceptionOccurred(e, this);
        }
    }

    private void calculateSizeToSend(Path path) {
        try {
            if (Files.isDirectory(path)) {
                size += SIZE_FOLDER;
                Files.list(path).forEach(this::calculateSizeToSend);
            } else {
                size += Files.size(path);
            }
        } catch (IOException e) {
            failure = true;
            if (statusSenderListener != null) statusSenderListener.anExceptionOccurred(e, this);
        }
    }
}
