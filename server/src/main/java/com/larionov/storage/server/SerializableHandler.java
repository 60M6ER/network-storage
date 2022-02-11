package com.larionov.storage.server;

import com.larionov.storage.core.auth.AuthorizationService;
import com.larionov.storage.core.files.FileViewer;
import com.larionov.storage.core.net.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class SerializableHandler extends SimpleChannelInboundHandler<Message> {

    private String login;
    private Path path;
    private FileViewer fileViewer;
    private AuthorizationService authorizationService;

    public SerializableHandler(Path path, AuthorizationService authorizationService) {
        this.path = path;
        this.authorizationService = authorizationService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("New accept user [" + ctx.channel().remoteAddress().toString() + "]");
        //ctx.writeAndFlush(new AuthorizationTrue("Не авторизованы"));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("User [" + ctx.channel().remoteAddress().toString() + "] disconnected");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        switch (message.getTypeMessage()) {
            case AUTH:
                AuthMessage authMessage = (AuthMessage) message;
                if (authorizationService.authorize(
                        authMessage.getLogin(),
                        authMessage.getPassword()
                )){
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
                fileViewer.createFolder(((CreateFolder) message).getNameFile());
                sendFileList(ctx);
                break;
            case RENAME:
                RenameFile renameMessage = (RenameFile) message;
                fileViewer.renameFile(renameMessage.getOldName(), renameMessage.getNewName());
                sendFileList(ctx);
                break;
            case DELETE:
                fileViewer.deleteFile(((DeleteFile) message).getNameFile());
                sendFileList(ctx);
                break;
            case OPEN_FOLDER:
                fileViewer.resolveFile(((OpenFolder) message).getNameFile());
                sendFileList(ctx);
                break;
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
}
