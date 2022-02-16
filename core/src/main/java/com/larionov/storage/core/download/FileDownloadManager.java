package com.larionov.storage.core.download;

import com.larionov.storage.core.download.exeptions.ManagerInUsed;
import com.larionov.storage.core.net.ErrorMessage;
import com.larionov.storage.core.net.ProcessedPackage;
import com.larionov.storage.core.net.SendDescriptionsMessage;
import com.larionov.storage.core.net.SendFile;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
public class FileDownloadManager {

    private UUID curTask;

    private Path pathFile;
    private long size = 0;
    private long sentBytes = 0;
    private boolean finished = true;

    private long timeStart;
    @Getter
    private String curFileName;
    private Path curPath;
    private OutputStream outputStream;
    private long curFileSentBytes;
    private int fileProgress;

    @Setter
    private StatusSenderListener listener;

    public FileDownloadManager(StatusSenderListener statusSenderListener) {
        this.listener = statusSenderListener;
    }

    public void newTransfer(SendDescriptionsMessage descriptionsMessage, Path path) {
        if (!finished) throw new ManagerInUsed();
        curTask = descriptionsMessage.getIdTransfer();
        size = descriptionsMessage.getSize();
        pathFile = path;
        timeStart = System.nanoTime();
        finished = false;
    }

    private void closeOutputStream() throws IOException {
        if (outputStream != null){
            outputStream.close();
        }
    }

    public void sendFile(SendFile sendFile) {
        if (finished) return;
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
            log.info("Write " + sendFile.getData().length + " bytes");
            sentBytes += sendFile.getData().length;
            curFileSentBytes += sendFile.getData().length;
            if (curFileSentBytes == sendFile.getSize())
                closeOutputStream();
        } catch (IOException e) {
            listener.newSendMessage(new ErrorMessage(e.getMessage(), e));
            e.printStackTrace();
            try {
                closeOutputStream();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
