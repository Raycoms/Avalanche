package com.constantine.communication;

import com.constantine.communication.message.MessageDecoder;
import com.constantine.communication.message.MessageEncoder;
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
import com.constantine.views.utils.ViewLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * The server sender class.
 */
public class ServerSender implements ISender
{
    /**
     * Map of servers ids to handlers to message them.
     */
    private final Map<Integer, NettySenderHandler> clients = new HashMap<>();

    /**
     * The view this server process has.
     */
    private final GlobalView view;

    /**
     * The server this process belongs to.
     */
    private final Server server;

    private final Bootstrap b;

    /**
     * Create the new client object and load its view.
     */
    public ServerSender(final String cfg, final Server server)
    {
        this.view = ViewLoader.loadView(cfg);
        this.server = server;

        b = new Bootstrap();
        final EventLoopGroup workerGroup = new NioEventLoopGroup();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY, true);
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
                      new MessageEncoder(),
                      new MessageDecoder(),
                      clientHandler);
                }
            });
            Log.getLogger().warn("Starting connection to ServerReceiver: " + data.getId());
            b.connect(data.getIp(), data.getPort());
        }
    }

    @Override
    public void unicast(final String message, final int id)
    {
        if (clients.containsKey(id))
        {
            clients.get(id).write(message);
        }
    }

    @Override
    public void multicast(final String message, final int... list)
    {
        for (final int id : list)
        {
            unicast(message, id);
        }
    }

    @Override
    public void broadcast(final String message)
    {
        for (final NettySenderHandler handler : clients.values())
        {
            handler.write(message);
        }
    }
}
