package com.larionov.storage.client;

import com.larionov.storage.core.net.AuthorizationTrue;
import com.larionov.storage.core.net.ErrorMessage;
import com.larionov.storage.core.net.FileList;
import com.larionov.storage.core.net.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientHandler extends SimpleChannelInboundHandler<Message> {
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
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        switch (message.getTypeMessage()) {
            case ERROR:
                listener.onError((ErrorMessage) message);
                break;
            case AUTH_TRUE:
                listener.onAuthorizationTrue((AuthorizationTrue) message);
                break;
            case FILE_LIST:
                listener.onFileList((FileList) message);
        }
    }
}
