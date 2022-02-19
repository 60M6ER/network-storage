package com.larionov.storage.core.download;

import com.larionov.storage.core.download.exeptions.ManagerInUsed;
import com.larionov.storage.core.net.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
public class FileDownloadManager implements TransferService{

    private UUID curTask;

    private Path pathFile;
    private long size = 0;
    private long sentBytes = 0;
    private boolean finished = true;

    private long timeStart;
    @Getter
    private String curFileName;
    @Getter
    private Path curPath;
    private OutputStream outputStream;
    private long curFileSentBytes;
    private int fileProgress;

    @Setter
    private TransferListener listener;

    public FileDownloadManager(Path path, TransferListener transferListener) {
        this.listener = transferListener;
        pathFile = path;
        timeStart = System.nanoTime();
        finished = false;
        listener.startProcess(this);
    }

    private void addInformationTransfer(SendDescriptionsMessage descriptionsMessage) {
        curTask = descriptionsMessage.getIdTransfer();
        size = descriptionsMessage.getSize();
        listener.startSendFiles(this);
    }

    private void createFolder(CreateFolder createFolder) throws IOException {
        if (finished) throw new IOException("First you need to send a transfer message");
        Path newFolder = pathFile.resolve(createFolder.getNameFile());
        Files.createDirectory(newFolder);
        listener.newSendMessage(new ProcessedPackage(true));
    }

    private void sendFile(SendFile sendFile) throws IOException {
        if (finished) throw new IOException("First you need to send a transfer message");
        try {
            if (curFileName == null || !curFileName.equals(sendFile.getPathFile())) {
                curFileName = sendFile.getPathFile();
                curPath = pathFile.resolve(curFileName);
                curFileSentBytes = 0;
                if (Files.exists(curPath))
                    Files.delete(curPath);
                Files.createFile(curPath);
                outputStream = Files.newOutputStream(curPath);
            }
            outputStream.write(sendFile.getData());
            outputStream.flush();
            listener.newSendMessage(new ProcessedPackage(true));
            sentBytes += sendFile.getData().length;
            curFileSentBytes += sendFile.getData().length;
            fileProgress = (int) (curFileSentBytes / sendFile.getSize() * 100);
            if (curFileSentBytes == sendFile.getSize())
                closeOutputStream();
        } catch (IOException e) {
            listener.newSendMessage(new ErrorMessage(e.getMessage(), e));
            e.printStackTrace();
            closeOutputStream();
            listener.anExceptionOccurred(e, this);
        }
    }

    private void closeOutputStream() throws IOException {
        if (outputStream != null){
            outputStream.close();
            listener.finishedDownload(this);
        }
    }

    @Override
    public void send() {

    }

    @Override
    public StatusSend getStatus() {
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
    public Path getPathFile() {
        return pathFile;
    }

    @Override
    public boolean isActive() {
        return !finished;
    }

    @Override
    public void stop() {
        finished = true;
    }

    @Override
    public void sendMessage(AbstractMessage message) {
        try {
        switch (message.getTypeMessage()){
            case CREATE_FOLDER:
                createFolder((CreateFolder) message);
                break;
            case SEND_FILE:
                sendFile((SendFile) message);
                break;
            case SEND_DESCRIPTIONS:
                addInformationTransfer((SendDescriptionsMessage) message);
        }
        } catch (IOException e) {
            listener.newSendMessage(new ProcessedPackage(false));
            listener.anExceptionOccurred(e, this);
            stop();
        }
    }
}
