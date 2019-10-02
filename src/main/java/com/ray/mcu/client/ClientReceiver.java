package com.ray.mcu.client;

import com.ray.mcu.nettyhandlers.SizedMessageDecoder;
import com.ray.mcu.nettyhandlers.SizedMessageEncoder;
import com.ray.mcu.utils.Log;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * Netty class to start a receiving client.
 */
public class ClientReceiver extends Thread
{
    /**
     * The client instance this client receiver is connected to.
     */
    private Client client;

    /**
     * Constructor of the receiver.
     * @param client the client details.
     */
    public ClientReceiver(final Client client)
    {
        this.client = client;
    }

    @Override
    public void run()
    {
        final NioEventLoopGroup acceptGroup = new NioEventLoopGroup();
        final NioEventLoopGroup connectGroup = new NioEventLoopGroup();

        final ClientData clientData = client.getClientData();
        try
        {
            final ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(acceptGroup, connectGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.localAddress(new InetSocketAddress(clientData.getIp(), clientData.getPort()));

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>()
            {
                protected void initChannel(SocketChannel socketChannel)
                {
                    socketChannel.pipeline().addLast(
                      new SizedMessageEncoder(),
                      new SizedMessageDecoder(),
                      new ClientNettyReceiverHandler(client));
                }
            });
            Log.getLogger().warn("Start accepting connections at Client: " + client.getClientData().getId());
            final ChannelFuture f = serverBootstrap.bind(clientData.getPort()).sync(); // (7)

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
