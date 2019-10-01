package com.constantine.comm.server.client;

import com.constantine.comm.client.ClientData;
import com.constantine.comm.communication.messages.IMessageWrapper;
import com.constantine.comm.nettyhandlers.SizedMessageDecoder;
import com.constantine.comm.nettyhandlers.SizedMessageEncoder;
import com.constantine.comm.server.IServer;
import com.constantine.comm.server.Server;
import com.constantine.comm.utils.Log;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * The server sender class.
 */
public class ServerClientSender extends Thread
{
    /**
     * Map of client public keys to handlers to handle them.
     */
    private final Map<PublicKey, ServerNettyClientSenderHandler> clients = new HashMap<>();

    /**
     * The server this process belongs to.
     */
    private final IServer server;

    /**
     * The server own bootstrap instance.
     */
    private final Bootstrap b;

    /**
     * Create the new client object and load its view.
     * @param server the server establishing the connection.
     */
    public ServerClientSender(final Server server)
    {
        this.server = server;

        b = new Bootstrap();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY, true);
    }

    @Override
    public void run()
    {
        while (server.isActive())
        {
            if (!server.hasMessageInClientOutputQueue())
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                continue;
            }
            server.consumeMessageFromClientOutputQueue().execute(this);
        }

        for (final ServerNettyClientSenderHandler handler : clients.values())
        {
            handler.disconnect();
        }
    }

    /**
     * Establish a connection to a client.
     * @param data the clien tdata.
     */
    public void connectToClient(final ClientData data)
    {
        if (clients.containsKey(data.getId()))
        {
            Log.getLogger().error(server.getServerData().getId() + ": Already created a connection to the server: " + data.getId() + " on this client!");
        }
        else
        {
            final ServerNettyClientSenderHandler clientHandler = new ServerNettyClientSenderHandler(server);
            clients.put(data.getId(), clientHandler);
            b.handler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                public void initChannel(SocketChannel ch)
                {
                    ch.pipeline().addLast(
                      new SizedMessageEncoder(),
                      new SizedMessageDecoder(),
                      clientHandler);
                }
            });
            Log.getLogger().warn("Starting connection to ServerReceiver: " + data.getId());
            b.connect(data.getIp(), data.getPort());
        }
    }

    /**
     * Disconnect a client.
     * @param data the client data.
     */
    public void disconnect(final ClientData data)
    {
        if (clients.containsKey(data.getId()))
        {
            final ServerNettyClientSenderHandler channel = clients.remove(data.getId());
            channel.disconnect();
        }
    }

    /**
     * Send a message to a certain client.
     * @param message the message to send.
     * @param id the clients unique id.
     */
    public void send(final IMessageWrapper message, final PublicKey id)
    {
        if (clients.containsKey(id))
        {
            final ServerNettyClientSenderHandler conn = clients.get(id);
            conn.write(message);
        }
    }
}
