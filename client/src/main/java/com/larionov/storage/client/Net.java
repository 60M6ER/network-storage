package com.larionov.storage.client;

import com.larionov.storage.core.net.AbstractMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Net {

    private NetListener listener;
    private SocketChannel channel;

    private String host;
    private Integer port;
    @Getter
    @Setter
    private boolean connected;

    private static Net INSTANCE;

    public static Net getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Net();
        }
        return INSTANCE;
    }

    private Net() {
        connected = false;
    }

    public void addListener(NetListener listener) {
        this.listener = listener;
        //Thread.sleep(500);
    }

    public void write(AbstractMessage msg) {
        channel.writeAndFlush(msg);
    }

    public void addHost(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void start() {
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class)
                    .group(worker)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            channel = socketChannel;
                            channel.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ClientHandler(listener)
                            );
                        }
                    });
            ChannelFuture future = bootstrap.connect(host, port)
                    .sync();
            log.info("client connected...");
            listener.onConnectionActive();
            connected = true;
            future.channel().closeFuture().sync(); // wait events
        } catch (Exception e) {
            log.error("e=", e);
        } finally {
            worker.shutdownGracefully();
        }
    }

    public void connect(){
        if (host.isEmpty() && port == null && port < 1)
            throw new RuntimeException("Host details are incorrect");
        Thread thread = new Thread(this::start);
        thread.setDaemon(true);
        thread.start();
    }
}
