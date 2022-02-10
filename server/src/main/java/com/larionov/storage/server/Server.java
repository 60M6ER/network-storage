package com.larionov.storage.server;

import com.larionov.storage.core.auth.AuthorizationService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class Server {
    private static final int PORT = 8189;

    private static final Path path = Paths.get("serverDir");
    private static AuthorizationService authorizationService;

    public static void main(String[] args) {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            authorizationService = new SimpleAuthorizationService(path);

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .group(auth, worker)
                    .childHandler(new SerializablePipeline(path, authorizationService));

            ChannelFuture future = serverBootstrap.bind(8189).sync();
            log.info("server started on port: " + PORT + "...");
            future.channel().closeFuture().sync();

        } catch (Exception e) {
            log.error("e=", e);
        }finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
