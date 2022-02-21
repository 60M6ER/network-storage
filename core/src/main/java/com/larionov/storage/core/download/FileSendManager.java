package com.larionov.storage.core.download;

import com.larionov.storage.core.download.exeptions.ManagerInUsed;
import com.larionov.storage.core.net.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Exchanger;

@Slf4j
public class FileSendManager implements TransferService{
    private static final Exchanger<Boolean> synchronizer = new Exchanger<>();

    private static final int BUFF_SIZE = 2048;

    private final UUID idTransfer = UUID.randomUUID();

    private Thread thread;
    private boolean failure = false;

    @Getter
    private final Path pathFile;
    private long size = 0;
    private long sentBytes = 0;

    private long timeStart;
    @Getter
    private String curFileName;
    private int fileProgress;

    @Setter
    private TransferListener transferListener;

    public FileSendManager(Path pathFile, TransferListener transferListener) {
        this.pathFile = pathFile;
        this.transferListener = transferListener;
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
                (long) ((double) (size - sentBytes) / sentBytes * statusSend.getTimePassed())
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

    @Override
    public boolean isActive() {
        return !failure && thread.isAlive();
    }

    @Override
    public void stop() {
        this.thread.interrupt();
    }

    @Override
    public void sendMessage(AbstractMessage message) {
        if (message.getTypeMessage() == TypeMessage.PROCESSED_PACKAGE) {
            try {
                synchronizer.exchange(((ProcessedPackage) message).isOK());
            } catch (InterruptedException interruptedException) {
                this.thread.interrupt();
            }
        }
    }

    private void start(){
        curFileName = pathFile.getFileName().toString();

        transferListener.startProcess(this);
        if (!thread.isInterrupted()) calculateSizeToSend(pathFile);
        transferListener.newSendMessage(new SendDescriptionsMessage(idTransfer, size));

        if (!failure) {
            transferListener.startSendFiles(this);
            timeStart = System.nanoTime();
            if (!thread.isInterrupted()) sendFiles(pathFile);
        }

        transferListener.finishedDownload(this);
    }

    private String getFilePath(Path curPath) throws IOException {
        if (Files.isSameFile(pathFile, curPath)){
            return curPath.getFileName().toString();
        } else {
            return curPath.toString().replace(pathFile.getParent().toString(), "");
        }
    }

    private void sendFiles(Path path) {
        try {
            if (thread.isInterrupted()) return;
            if (Files.isDirectory(path)) {
                curFileName = getFilePath(path);
                fileProgress = 100;
                transferListener.newSendMessage(new CreateFolder(
                        true,
                        curFileName
                ));
                if (!synchronizer.exchange(null)) throw new IOException("the server was unable to process the packet");
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
                    transferListener.newSendMessage(new SendFile(
                            curFileName,
                            sizeCurFile,
                            data
                    ));
                    if (!synchronizer.exchange(null)) throw new IOException("the server was unable to process the packet");
                    sentBytes += data.length;
                    fileProgress = (int) ((sizeCurFile - available) * 100 / sizeCurFile);
                    available = inputStream.available();
                }
            }
        } catch (IOException | InterruptedException e) {
            failure = true;
            thread.interrupt();
            transferListener.anExceptionOccurred(e, this);
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
            if (transferListener != null) transferListener.anExceptionOccurred(e, this);
        }
    }
}
