package com.constantine.server.client;

import com.constantine.nettyhandlers.SizedMessageDecoder;
import com.constantine.nettyhandlers.SizedMessageEncoder;
import com.constantine.server.Server;
import com.constantine.server.ServerData;
import com.constantine.utils.Log;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * Netty class to start a receiving server.
 */
public class ServerClientReceiver extends Thread
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
    public ServerClientReceiver(final Server server)
    {
        this.server = server;
    }

    @Override
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
            serverBootstrap.localAddress(new InetSocketAddress(serverData.getIp(), serverData.getCport()));

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>()
            {
                protected void initChannel(SocketChannel socketChannel)
                {
                    socketChannel.pipeline().addLast(
                      new SizedMessageEncoder(),
                      new SizedMessageDecoder(),
                      new ServerNettyClientReceiverHandler(server));
                }
            });
            Log.getLogger().warn("Start accepting connections from Clients at Server: " + server.getServerData().getId());
            f = serverBootstrap.bind(serverData.getCport()).sync(); // (7)

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
