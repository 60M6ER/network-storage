package com.larionov.storage.client;

import com.larionov.storage.core.net.AuthorizationTrue;
import com.larionov.storage.core.net.ErrorMessage;
import com.larionov.storage.core.net.FileList;
import com.larionov.storage.core.net.AbstractMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<AbstractMessage> {
    private NetListener listener;

    public ClientHandler(NetListener listener) {
        this.listener = listener;
    }

    public void setCallback(NetListener listener) {
        this.listener = listener;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //listener.onConnectionActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Net.getInstance().setConnected(false);
        listener.onConnectionInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        listener.onError(new ErrorMessage(cause.getMessage()));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage abstractMessage) throws Exception {
        switch (abstractMessage.getTypeMessage()) {
            case ERROR:
                listener.onError((ErrorMessage) abstractMessage);
                break;
            case AUTH_TRUE:
                listener.onAuthorizationTrue((AuthorizationTrue) abstractMessage);
                break;
            case FILE_LIST:
                listener.onFileList((FileList) abstractMessage);
        }
    }
}
