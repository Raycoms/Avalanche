package com.constantine.launcher;

import com.constantine.communication.messages.JoinRequestMessageWrapper;
import com.constantine.communication.messages.UnregisterRequestMessageWrapper;
import com.constantine.communication.serveroperations.UnicastOperation;
import com.constantine.server.Server;
import com.constantine.server.ServerData;
import com.constantine.utils.KeyUtilities;
import com.constantine.utils.Log;
import com.constantine.views.GlobalView;
import com.constantine.views.utils.ViewLoader;

import static com.constantine.utils.Constants.CONFIG_LOCATION;

/**
 * Class to launch one server instance per server in the view.
 * This is only useful for local tests on a single computer.
 */
public class ServerLauncher
{
    /**
     * Start an instance of a server
     *
     * @param args the arguments of the server (id, ip, host)
     */
    public static void main(final String[] args)
    {
        final GlobalView view = ViewLoader.loadView(CONFIG_LOCATION, "view.json");
        KeyUtilities.generateOrLoadKeys(view.getServers(), CONFIG_LOCATION);

        for (final ServerData server : view.getServers())
        {
            final Server thread = new Server(server.getId(), server.getIp(), server.getPort());
            thread.start();
        }

        try
        {
            simulate(view);
        }
        catch (final InterruptedException e)
        {
            /*
             * Nothing to do here.
             */
        }
    }

    /**
     * Simulate different behaviours of the group.
     *
     * Starts a new server which will request entry.
     * Kills server off by force.
     * Restarts server. (Other servers should reconnect).
     * Request unregistering and then turns off.
     *
     * @param view the view this is executed on.
     * @throws InterruptedException when the threads sleep.
     */
    private static void simulate(final GlobalView view) throws InterruptedException
    {
        Thread.sleep(10000);

        Log.getLogger().error("----------------------------------------------------------");
        Log.getLogger().error("Starting up new Replica");
        Log.getLogger().error("----------------------------------------------------------");

        final Server server1 = new Server(4, "localhost", 8085);
        server1.start();

        Thread.sleep(10000);

        Log.getLogger().error("----------------------------------------------------------");
        Log.getLogger().error("Force shutdown on replica");
        Log.getLogger().error("----------------------------------------------------------");


        server1.isActive.set(false);

        Thread.sleep(10000);

        Log.getLogger().error("----------------------------------------------------------");
        Log.getLogger().error("Startup and register replica again");
        Log.getLogger().error("----------------------------------------------------------");


        final Server server2 = new Server(4, "localhost", 8085);
        server2.start();

        Thread.sleep(10000);

        Log.getLogger().error("----------------------------------------------------------");
        Log.getLogger().error("Unregister replica this time");
        Log.getLogger().error("----------------------------------------------------------");

        server2.outputQueue.add(new UnicastOperation(new UnregisterRequestMessageWrapper(server2, server2.getServerData()), view.getCoordinator()));

        Thread.sleep(10000);

        Log.getLogger().error("----------------------------------------------------------");
        Log.getLogger().error("Force shutdown on replica");
        Log.getLogger().error("----------------------------------------------------------");

        server1.isActive.set(false);

        //todo simulate sleep start clients, etc.
    }
}
