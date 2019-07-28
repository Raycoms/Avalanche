package com.constantine.communication;

import com.constantine.communication.message.MessageDecoder;
import com.constantine.communication.message.MessageEncoder;
import com.constantine.server.Server;
import com.constantine.utils.Log;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import com.constantine.server.ServerData;

import java.net.InetSocketAddress;

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
    public void run()
    {
        final NioEventLoopGroup acceptGroup = new NioEventLoopGroup();
        final NioEventLoopGroup connectGroup = new NioEventLoopGroup();

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
                      new MessageEncoder(),
                      new MessageDecoder(),
                      new NettyReceiverHandler(server));
                }
            });
            Log.getLogger().warn("Start accepting connections at Server: " + server.getId());
            final ChannelFuture f = serverBootstrap.bind(serverData.getPort()).sync(); // (7)

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
}
