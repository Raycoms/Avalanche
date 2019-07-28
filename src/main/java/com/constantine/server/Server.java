package com.constantine.server;

import com.constantine.communication.ServerReceiver;
import com.constantine.communication.ServerSender;
import com.constantine.utils.KeyUtilities;
import com.constantine.utils.Log;
import java.security.PrivateKey;

import static com.constantine.utils.Constants.CONFIG_LOCATION;

/**
 * ServerReceiver representation in View.
 */
public class Server extends Thread
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
     * Create a server object.
     * @param id the server id.
     * @param ip the server ip.
     * @param port the server port.
     */
    public Server(final int id, final String ip, final int port)
    {
        this.server = new ServerData(id, ip, port);
        this.privateKey = KeyUtilities.loadPrivateKeyFromFile(CONFIG_LOCATION, this.server);
    }

    @Override
    public void run()
    {
        Log.getLogger().warn("Starting Server Thread for Server: " + server.getId());
        // This is an extra thread to start this async.
        final ServerReceiver receiver = new ServerReceiver(this);
        receiver.start();

        final ServerSender sender = new ServerSender(CONFIG_LOCATION + "view.json", this);
        sender.startUp();

        int nextId = server.getId() + 1;
        if (nextId >= 4)
        {
            nextId = 0;
        }
        sender.unicast("go", nextId);
    }

    /**
     * Getter for the ServerData,
     * @return the ServerData.
     */
    public ServerData getServerData()
    {
        return server;
    }

    /**
     * Sign a message with the private key of this server.
     * @param message the message to sign.
     * @return the resulting signature.
     */
    public byte[] signMessage(final byte[] message)
    {
        return KeyUtilities.signMessage(message, this.privateKey);
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
