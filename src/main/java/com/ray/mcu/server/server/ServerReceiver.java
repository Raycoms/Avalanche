package com.ray.mcu.server.server;

import com.ray.mcu.nettyhandlers.SizedMessageDecoder;
import com.ray.mcu.nettyhandlers.SizedMessageEncoder;
import com.ray.mcu.server.Server;
import com.ray.mcu.utils.Log;
import com.ray.mcu.server.ServerData;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Netty class to start a receiving server.
 */
public class ServerReceiver extends Thread
{
    /**
     * The server instance this server receiver is connected to.
     */
    private Server server;

    /**
     * The channel future after connection.
     */
    private ChannelFuture f;

    /**
     * Constructor of the receiver.
     * @param server the server details.
     */
    public ServerReceiver(final Server server)
    {
        this.server = server;
    }

    /**
     * Method to startup the receiver.
     */
    @Override
    public void run()
    {
        final NioEventLoopGroup acceptGroup = new NioEventLoopGroup(0, Executors.newCachedThreadPool());
        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(0, Executors.newCachedThreadPool());

        final ServerData serverData = server.getServerData();
        try
        {
            final ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(acceptGroup, connectGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(new InetSocketAddress(serverData.getIp(), serverData.getPort()));

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>()
            {
                protected void initChannel(SocketChannel socketChannel)
                {
                    socketChannel.pipeline().addLast(
                      new SizedMessageEncoder(),
                      new SizedMessageDecoder(),
                      new ServerNettyReceiverHandler(server));
                }
            });
            Log.getLogger().warn("Start accepting connections at Server: " + server.getServerData().getId());
            f = serverBootstrap.bind(serverData.getPort()).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            acceptGroup.shutdownGracefully();
            connectGroup.shutdownGracefully();
        }
    }

    /**
     * Disconnect the receiver too.
     */
    public void disconnect()
    {
        f.channel().disconnect();
    }
}
