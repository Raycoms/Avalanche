package com.ray.mcu.server.server;

import com.ray.mcu.communication.ISender;
import com.ray.mcu.nettyhandlers.SizedMessageDecoder;
import com.ray.mcu.nettyhandlers.SizedMessageEncoder;
import com.ray.mcu.communication.wrappers.IMessageWrapper;
import com.ray.mcu.communication.serveroperations.IOperation;
import com.ray.mcu.communication.recovery.ReconnectThread;
import com.ray.mcu.server.IServer;
import com.ray.mcu.server.Server;
import com.ray.mcu.server.ServerData;
import com.ray.mcu.utils.Log;
import com.ray.mcu.views.GlobalView;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

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
    private final Map<Integer, ServerNettySenderHandler> clients = new HashMap<>();

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

        while (server.isActive())
        {
            if (!server.hasMessageInOutputQueue())
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

        for (final ServerNettySenderHandler handler : clients.values())
        {
            handler.disconnect();
        }
    }

    /**
     * Handle a message from the queue.
     * @param message the message to send.
     */
    public void handleMessage(final IOperation message)
    {
        Log.getLogger().warn(server.getServerData().getId() + ": Sending message");
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
            for (final ServerNettySenderHandler handler : clients.values())
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
            Log.getLogger().error(server.getServerData().getId() + ": Already created a connection to the server: " + data.getId() + " on this client!");
        }
        else
        {
            final ServerNettySenderHandler clientHandler = new ServerNettySenderHandler(server, data);
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

    @Override
    public void disconnectFromServer(final ServerData data)
    {
        if (clients.containsKey(data.getId()))
        {
            final ServerNettySenderHandler channel = clients.remove(data.getId());
            channel.disconnect();
        }
    }

    @Override
    public void unicast(final IMessageWrapper message, final int id)
    {
        if (clients.containsKey(id))
        {
            final ServerNettySenderHandler conn = clients.get(id);
            if (!conn.write(message))
            {
                Log.getLogger().warn("Unable to write");
                if (!conn.isReconnecting())
                {
                    new ReconnectThread(clients.get(id), b).start();
                }
            }
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
        for (final ServerNettySenderHandler handler : clients.values())
        {
            unicast(message, handler.getServerData().getId());
        }
    }
}
