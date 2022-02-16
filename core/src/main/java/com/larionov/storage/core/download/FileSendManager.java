package com.larionov.storage.core.download;

import com.larionov.storage.core.download.exeptions.ManagerInUsed;
import com.larionov.storage.core.net.CreateFolder;
import com.larionov.storage.core.net.SendDescriptionsMessage;
import com.larionov.storage.core.net.SendFile;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Exchanger;

@Slf4j
public class FileSendManager {
    @Getter
    private static final Exchanger<Boolean> synchronizer = new Exchanger<>();

    private static final int BUFF_SIZE = 2048;

    @Getter
    private final UUID idTransfer = UUID.randomUUID();

    @Getter
    private Thread thread;
    private boolean failure = false;

    private final Path pathFile;
    private long size = 0;
    private long sentBytes = 0;

    private long timeStart;
    @Getter
    private String curFileName;
    private int fileProgress;

    @Setter
    private StatusSenderListener statusSenderListener;

    public FileSendManager(Path pathFile, StatusSenderListener statusSenderListener) {
        this.pathFile = pathFile;
        this.statusSenderListener = statusSenderListener;
    }

    public void send() {
        if (thread == null) {
            thread = new Thread(this::start);
            thread.setPriority(Thread.MIN_PRIORITY);
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
        statusSend.setTimeLeft(sentBytes > 0 ?
                (size - sentBytes) * statusSend.getTimePassed() / sentBytes
                : 0);
        statusSend.setGlobalProgress(sentBytes * 1.0 / size);

        statusSend.setMessageStatus(String.format(
                "%s -> %s File: %s %s%% %s/%s",
                FileUtilities.timesToString(statusSend.getTimePassed()),
                FileUtilities.timesToString(statusSend.getTimeLeft()),
                curFileName,
                fileProgress,
                FileUtilities.bytesToString(sentBytes),
                FileUtilities.bytesToString(size)
        ));
        return statusSend;
    }

    private void start(){
        curFileName = pathFile.getFileName().toString();

        statusSenderListener.startProcess(this);
        if (!thread.isInterrupted()) calculateSizeToSend(pathFile);
        statusSenderListener.newSendMessage(new SendDescriptionsMessage(idTransfer, size));

        if (!failure) {
            statusSenderListener.startSendFiles(this);
            timeStart = System.nanoTime();
            if (!thread.isInterrupted()) sendFiles(pathFile);
        }

        statusSenderListener.finishedDownload(this);
    }

    private String getFilePath(Path curPath) throws IOException {
        if (Files.isSameFile(pathFile, curPath)){
            return curPath.getFileName().toString();
        } else {
            return curPath.toString().replace(pathFile.toString(), "");
        }
    }

    private void sendFiles(Path path) {
        try {
            if (thread.isInterrupted()) return;
            if (Files.isDirectory(path)) {
                curFileName = path.getFileName().toString();
                fileProgress = 100;
                statusSenderListener.newSendMessage(new CreateFolder(
                        true,
                        curFileName
                ));
                if (!synchronizer.exchange(null)) throw new IOException("the server was unable to process the packet");
                if (statusSenderListener != null) statusSenderListener.sendStatus(this);
                Files.list(path).forEach(this::sendFiles);
            } else {
                long sizeCurFile = Files.size(path);
                curFileName = getFilePath(path);
                InputStream inputStream = Files.newInputStream(path);
                int available = inputStream.available();
                byte[] data = new byte[Math.min(available, BUFF_SIZE)];
                while (available > 0 && !thread.isInterrupted()) {

                    int read = inputStream.read(data);

                    if (read == 0) throw new RuntimeException("Failed to read file");
                    if (read < data.length){
                        data = Arrays.copyOf(data, read);
                    }
                    statusSenderListener.newSendMessage(new SendFile(
                            curFileName,
                            sizeCurFile,
                            data
                    ));
                    if (!synchronizer.exchange(null)) throw new IOException("the server was unable to process the packet");
                    sentBytes += data.length;
                    fileProgress = (int) ((sizeCurFile - available) * 100 / sizeCurFile);
                    statusSenderListener.sendStatus(this);
                    available = inputStream.available();
                }
            }
        } catch (IOException | InterruptedException e) {
            failure = true;
            thread.interrupt();
            statusSenderListener.anExceptionOccurred(e, this);
        }
    }

    private void calculateSizeToSend(Path path) {
        try {
            if (Files.isDirectory(path)) {
                Files.list(path).forEach(this::calculateSizeToSend);
            } else {
                size += Files.size(path);
            }
        } catch (IOException e) {
            failure = true;
            thread.interrupt();
            if (statusSenderListener != null) statusSenderListener.anExceptionOccurred(e, this);
        }
    }
}
