package com.larionov.storage.server;

import com.larionov.storage.core.auth.AuthorizationService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.nio.file.Path;

public class SerializablePipeline extends ChannelInitializer<SocketChannel> {

    private Path path;
    private AuthorizationService authorizationService;

    public SerializablePipeline(Path path, AuthorizationService authorizationService) {
        this.path = path;
        this.authorizationService = authorizationService;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline()
                .addLast(
                        new ObjectEncoder(),
                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                        new SerializableHandler(path, authorizationService)
                );
    }
}
