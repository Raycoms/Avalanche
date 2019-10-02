package com.ray.mcu.server;

import com.ray.mcu.communication.MessageHandlerRegistry;
import com.ray.mcu.communication.clientoperations.IClientOperation;
import com.ray.mcu.communication.wrappers.IMessageWrapper;
import com.ray.mcu.communication.wrappers.JoinRequestMessageWrapper;
import com.ray.mcu.server.client.ClientMessageHandler;
import com.ray.mcu.server.client.ServerClientReceiver;
import com.ray.mcu.server.client.ServerClientSender;
import com.ray.mcu.server.server.ServerMessageHandler;
import com.ray.mcu.server.server.ServerReceiver;
import com.ray.mcu.server.server.ServerSender;
import com.constantine.comm.communication.messages.*;
import com.ray.mcu.communication.serveroperations.IOperation;
import com.ray.mcu.communication.serveroperations.UnicastOperation;
import com.ray.mcu.utils.KeyUtilities;
import com.ray.mcu.utils.Log;
import com.ray.mcu.views.GlobalView;
import com.ray.mcu.views.utils.ViewLoader;
import com.ray.mcu.utils.Constants;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ServerReceiver representation in View.
 */
public class Server extends Thread implements IServer
{
    /**
     * The server data.
     */
    private final ServerData server;

    /**
     * The private key which belongs to this server replica.
     */
    private PrivateKey privateKey;

    /**
     * Cache which holds the receives messages (Consumed by Server)
     */
    public final ConcurrentLinkedQueue<IMessageWrapper> inputQueue = new ConcurrentLinkedQueue<>();

    /**
     * Cache which holds the receives messages (Consumed by Server)
     */
    public final ConcurrentLinkedQueue<IMessageWrapper> clientInputQueue = new ConcurrentLinkedQueue<>();

    /**
     * Cache which holds the messages to send in the future (Produced by Server).
     */
    public final ConcurrentLinkedQueue<IOperation> outputQueue = new ConcurrentLinkedQueue<>();

    /**
     * Cache which holds the messages to send in the future (Produced by Server).
     */
    public final ConcurrentLinkedQueue<IClientOperation> clientOutputQueue = new ConcurrentLinkedQueue<>();

    /**
     * The global view this server uses.
     */
    public final GlobalView view;

    /**
     * The current state of our servers. Client Public Key to Integer account balance.
     */
    public final HashMap<PublicKey, Integer> state = new HashMap<>();

    /**
     * Var setting the server to be active.
     */
    public AtomicBoolean isActive = new AtomicBoolean(true);

    /**
     * Create a server object.
     * @param id the server id.
     * @param ip the server ip.
     * @param port the server port.
     */
    public Server(final int id, final String ip, final int port)
    {
        this.server = new ServerData(id, ip, port);
        KeyUtilities.generateOrLoadKey(server, Constants.CONFIG_LOCATION);
        this.privateKey = KeyUtilities.loadPrivateKeyFromFile(Constants.CONFIG_LOCATION, this.server);
        this.view = ViewLoader.loadView(Constants.CONFIG_LOCATION,  "view.json");
        for (final ServerData data: view.getServers())
        {
            data.loadPublicKey(Constants.CONFIG_LOCATION);
        }
    }

    @Override
    public void run()
    {
        Log.getLogger().warn("Starting Server Thread for Server: " + server.getId());
        boolean isInView = true;
        if (view.getServer(server.getId()) == null)
        {
            view.addServer(server);
            isInView = false;
        }

        // This is an extra thread to start this async.
        final ServerReceiver receiver = new ServerReceiver(this);
        receiver.start();

        final ServerClientReceiver clientReceiver = new ServerClientReceiver(this);
        clientReceiver.start();

        final ServerSender sender = new ServerSender(view, this);
        sender.start();

        final ServerClientSender clientSender = new ServerClientSender(this);
        clientSender.start();

        if (!isInView)
        {
            outputQueue.add(new UnicastOperation(new JoinRequestMessageWrapper(this, server), view.getCoordinator()));
        }

        final ServerMessageHandler serverMessageHandler = new ServerMessageHandler(this);
        serverMessageHandler.start();

        final ClientMessageHandler clientMessageHandler = new ClientMessageHandler(this);
        clientMessageHandler.start();

        while (isActive.get())
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                /*
                 * Nothing to do here.
                 */
            }
        }

        Log.getLogger().warn("Detected inactive!");
        receiver.disconnect();
        clientReceiver.disconnect();
    }

    @Override
    public void handleMessage(final IMessageWrapper message)
    {
        MessageHandlerRegistry.handle(message, this);
    }

    @Override
    public ServerData getServerData()
    {
        return server;
    }

    @Override
    public byte[] signMessage(final byte[] message)
    {
        return KeyUtilities.signMessage(message, this.privateKey);
    }

    @Override
    public boolean hasMessageInOutputQueue()
    {
        return !outputQueue.isEmpty();
    }

    @Override
    public boolean hasMessageInClientOutputQueue()
    {
        return !clientOutputQueue.isEmpty();
    }

    @Override
    public IOperation consumeMessageFromOutputQueue()
    {
        return outputQueue.poll();
    }

    @Override
    public IClientOperation consumeMessageFromClientOutputQueue()
    {
        return clientOutputQueue.poll();
    }

    @Override
    public boolean isActive()
    {
        return isActive.get();
    }

    /**
     * Start an instance of a server
     * @param args the arguments of the server (id, ip, host)
     */
    public static void main(final String[] args)
    {
        if (args.length < 3)
        {
            Log.getLogger().warn("Invalid arguments, at least 3 necessary!");
            return;
        }

        final int id = Integer.parseInt(args[0]);
        final String ip = args[1];
        final int port = Integer.parseInt(args[2]);

        final Server server = new Server(id, ip, port);
        server.start();
    }
}
