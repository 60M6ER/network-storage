package com.larionov.storage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class Server {
    private static final int PORT = 8189;

    public static void main(String[] args) throws IOException {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .group(auth, worker)
                    .childHandler(new EchoStringPipeLine());

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
