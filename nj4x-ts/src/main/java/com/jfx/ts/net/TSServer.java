package com.jfx.ts.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.IOException;

public final class TSServer {

    private final ChannelFuture channelFuture;
    private final NioEventLoopGroup bossGroup;
    private final NioEventLoopGroup workerGroup;
    private TS ts;

    @SuppressWarnings("unused")
    public TSServer(final TS ts) throws IOException {
        this.ts = ts;
        // Configure the server.
        bossGroup = new NioEventLoopGroup(2);
        workerGroup = new NioEventLoopGroup(Math.max(1, Runtime.getRuntime().availableProcessors() * 2 / 3 ));//num CPU * 2 threads by dflt
//        workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        //p.addLast(new LoggingHandler(LogLevel.INFO));
                        p.addLast(new LineProtocol());
                        p.addLast(new ClientWorkerHdlr(TSServer.this.ts));
                    }
                });

        // Start the server.
        try {
            channelFuture = b.bind(ts.getPort()).sync();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }

    }

    public void waitForCompletion() throws InterruptedException {
        try {
            // Wait until the server socket is closed.
            channelFuture.channel().closeFuture().sync();
        } finally {
            shutDown();
        }
    }

    public void shutDown() {
        // Shut down all event loops to terminate all threads.
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}