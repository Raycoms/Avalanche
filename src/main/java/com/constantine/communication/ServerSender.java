package com.constantine.communication;

import com.constantine.communication.handlers.SignedSizedMessageEncoder;
import com.constantine.communication.handlers.SizedMessageDecoder;
import com.constantine.communication.handlers.SizedMessageEncoder;
import com.constantine.communication.messages.IMessageWrapper;
import com.constantine.communication.operations.IOperation;
import com.constantine.server.Server;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import com.constantine.server.ServerData;
import com.constantine.utils.Log;
import com.constantine.views.GlobalView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The server sender class.
 */
public class ServerSender extends Thread implements ISender
{
    /**
     * Map of servers ids to handlers to handlers them.
     */
    private final Map<Integer, NettySenderHandler> clients = new HashMap<>();

    /**
     * The view this server process has.
     */
    private final GlobalView view;

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
     * @param view the view of the network to connect to.
     */
    public ServerSender(final GlobalView view, final Server server)
    {
        this.view = view;
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
        startUp();

        while (true)
        {
            if (server.hasMessageInOutputQueue())
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
            handleMessage(server.consumeMessageFromOutputQueue());
        }
    }

    /**
     * Handle a message from the queue.
     * @param message the message to send.
     */
    public void handleMessage(final IOperation message)
    {
        message.executeOP(this);
    }

    @Override
    public void startUp()
    {
        for (final ServerData serverData : view.getServers())
        {
            connectToServer(serverData);
        }

        Log.getLogger().warn("Wait a second for netty to start up");
        boolean allActive = false;
        while (!allActive)
        {
            allActive = true;
            for (final NettySenderHandler handler : clients.values())
            {
                if (!handler.isActive())
                {
                    allActive = false;
                }
            }

            if (!allActive)
            {
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        Log.getLogger().warn("All Netty clients succesfully started");
    }

    @Override
    public void connectToServer(final ServerData data)
    {
        if (clients.containsKey(data.getId()))
        {
            Log.getLogger().error("Already created a connection to the server: " + data.getId() + " on this client!");
        }
        else
        {
            final NettySenderHandler clientHandler = new NettySenderHandler(server, data);
            clients.put(data.getId(), clientHandler);
            b.handler(new ChannelInitializer<SocketChannel>()
            {
                @Override
                public void initChannel(SocketChannel ch)
                {
                    ch.pipeline().addLast(
                      new SignedSizedMessageEncoder(),
                      new SizedMessageEncoder(),
                      new SizedMessageDecoder(),
                      clientHandler);
                }
            });
            Log.getLogger().warn("Starting connection to ServerReceiver: " + data.getId());
            b.connect(data.getIp(), data.getPort());
        }
    }

    @Override
    public void unicast(final IMessageWrapper message, final int id)
    {
        if (clients.containsKey(id))
        {
            clients.get(id).write(message);
        }
    }

    @Override
    public void multicast(final IMessageWrapper message, final List<Integer> list)
    {
        for (final int id : list)
        {
            unicast(message, id);
        }
    }

    @Override
    public void broadcast(final IMessageWrapper message)
    {
        for (final NettySenderHandler handler : clients.values())
        {
            handler.write(message);
        }
    }
}
