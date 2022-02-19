package com.larionov.storage.server;

import com.larionov.storage.core.auth.AuthorizationService;
import com.larionov.storage.core.download.FileDownloadManager;
import com.larionov.storage.core.download.FileSendManager;
import com.larionov.storage.core.download.TransferListener;
import com.larionov.storage.core.download.TransferService;
import com.larionov.storage.core.download.exeptions.ErrorReceiveFile;
import com.larionov.storage.core.download.exeptions.ManagerInUsed;
import com.larionov.storage.core.files.FileViewer;
import com.larionov.storage.core.net.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class SerializableHandler extends SimpleChannelInboundHandler<AbstractMessage> implements TransferListener {

    private String login;
    private Path path;
    private FileViewer fileViewer;
    private AuthorizationService authorizationService;
    private TransferService transferService;
    private Channel channel;

    public SerializableHandler(Path path, AuthorizationService authorizationService) {
        this.path = path;
        this.authorizationService = authorizationService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("New accept user [" + ctx.channel().remoteAddress().toString() + "]");
        //ctx.writeAndFlush(new AuthorizationTrue("Не авторизованы"));
        channel = ctx.channel();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("User [" + ctx.channel().remoteAddress().toString() + "] disconnected");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage abstractMessage) throws Exception {
        try {
            switch (abstractMessage.getTypeMessage()) {
                case AUTH:
                    AuthMessage authMessage = (AuthMessage) abstractMessage;
                    if (authorizationService.authorize(
                            authMessage.getLogin(),
                            authMessage.getPassword()
                    )) {
                        login = authMessage.getLogin();
                        log.info("Authorized user with login: " + login);
                        createFileViewer();
                        ctx.writeAndFlush(new AuthorizationTrue(""));
                        sendFileList(ctx);
                    } else {
                        ctx.writeAndFlush(new ErrorMessage("Wrong login or password."));
                    }
                    break;
                case CREATE_FOLDER:
                    CreateFolder createFolder = (CreateFolder) abstractMessage;
                    if (createFolder.isSendingFile()){
                        transferService.sendMessage(createFolder);
                    }else {
                        fileViewer.createFolder(createFolder.getNameFile());
                        if (!createFolder.isSendingFile())
                            sendFileList(ctx);
                    }
                    break;
                case RENAME:
                    RenameFile renameMessage = (RenameFile) abstractMessage;
                    fileViewer.renameFile(renameMessage.getOldName(), renameMessage.getNewName());
                    sendFileList(ctx);
                    break;
                case DELETE:
                    fileViewer.deleteFile(((DeleteFile) abstractMessage).getNameFile());
                    sendFileList(ctx);
                    break;
                case OPEN_FOLDER:
                    fileViewer.resolveFile(((OpenFolder) abstractMessage).getNameFile());
                    sendFileList(ctx);
                    break;
                case SEND_DESCRIPTIONS:
                    if (transferService == null || !transferService.isActive()){
                        transferService = new FileDownloadManager(
                                fileViewer.getCurrentDir(),
                                this
                        );
                        transferService.sendMessage(abstractMessage);
                    }else {
                        throw new ManagerInUsed();
                    }
                    break;
                case SEND_FILE:
                    if (transferService == null || !transferService.isActive()) throw new RuntimeException("Create new manager");
                    transferService.sendMessage(abstractMessage);
                    break;
                case QUERY_FILE_LIST:
                    sendFileList(ctx);
                    break;
                case QUERY_FILE:
                    if (transferService != null && transferService.isActive()) throw new RuntimeException("Create new manager");
                    transferService = new FileSendManager(
                            fileViewer.getPathToFile(((QueryFile) abstractMessage).getNameFile()),
                            this
                    );
                    transferService.send();
                    break;
                case PROCESSED_PACKAGE:
                    if (transferService == null || !transferService.isActive()) throw new RuntimeException("No suitable manager");
                    transferService.sendMessage(abstractMessage);
            }
        } catch (Exception e) {
            if (abstractMessage instanceof CreateFolder && ((CreateFolder) abstractMessage).isSendingFile()
                    || abstractMessage instanceof SendFile
                    || abstractMessage instanceof SendDescriptionsMessage
                    || abstractMessage instanceof QueryFile
                    || abstractMessage instanceof ProcessedPackage) {
                    ctx.writeAndFlush(new ErrorMessage(e.getMessage(),
                            new ErrorReceiveFile(e.getMessage())));
            }
            ctx.writeAndFlush(new ErrorMessage(e.getMessage(), e));
        }
    }

    private void createFileViewer() throws IOException {
        Path userDirectory = path.resolve(login);
        if (!Files.exists(userDirectory))
            Files.createDirectory(userDirectory);
        fileViewer = new FileViewer(userDirectory);
    }

    private void sendFileList(ChannelHandlerContext ctx) throws IOException {
        ctx.writeAndFlush(new FileList(fileViewer.getViewDir(), fileViewer.getListFiles()));
    }

    @Override
    public void startProcess(TransferService transferService) {
        log.info("Start transfer file for user: " + login);
    }

    @Override
    public void startSendFiles(TransferService transferService) {

    }

    @Override
    public void finishedDownload(TransferService transferService) {
        log.info("Finished transfer file for user: " + login);
        this.transferService = null;
    }

    @Override
    public void anExceptionOccurred(Exception e, TransferService fileSendManager) {
        channel.writeAndFlush(new ErrorMessage(e.getMessage(), e));
    }

    @Override
    public void newSendMessage(AbstractMessage message) {
        channel.writeAndFlush(message);
    }
}
