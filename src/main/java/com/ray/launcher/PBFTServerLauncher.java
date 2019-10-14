package com.ray.launcher;

import com.ray.mcu.client.Client;
import com.ray.mcu.server.Server;
import com.ray.mcu.server.ServerData;
import com.ray.mcu.utils.Constants;
import com.ray.mcu.utils.KeyUtilities;
import com.ray.mcu.utils.ViewLoader;
import com.ray.mcu.views.GlobalView;
import com.ray.pbft.server.PbftServer;
import sun.security.rsa.RSAKeyPairGenerator;

/**
 * Class to launch one server instance per server in the view.
 * This is only useful for local tests on a single computer.
 */
public class PBFTServerLauncher
{
    /**
     * Start an instance of a server
     *
     * @param args the arguments of the server (id, ip, host)
     */
    public static void main(final String[] args)
    {
        final GlobalView view = ViewLoader.loadView(Constants.CONFIG_LOCATION, "view.json");
        KeyUtilities.generateOrLoadKeys(view.getServers(), Constants.CONFIG_LOCATION);

        for (final ServerData server : view.getServers())
        {
            final PbftServer thread = new PbftServer(server.getId(), server.getIp(), server.getPort());
            thread.start();
        }

        try
        {
            simulate(view);
        }
        catch (final InterruptedException e)
        {
            //Nothing to do here.
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
        Thread.sleep(5000);

        /*Log.getLogger().error("----------------------------------------------------------");
        Log.getLogger().error("Starting up new Replica");
        Log.getLogger().error("----------------------------------------------------------");

        final Server server1 = new Server(4, "localhost", 8088);
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
        server2.view.addServer(server2.getServerData());
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

        Thread.sleep(10000);

        Log.getLogger().error("----------------------------------------------------------");
        Log.getLogger().error("Startup clients");
        Log.getLogger().error("----------------------------------------------------------");

        */
        final RSAKeyPairGenerator gen = new RSAKeyPairGenerator();

        Client.createClient(0, gen, "localhost", 6000);
        //Client.createClient(0, gen, "localhost", 6001);
        //Client.createClient(0, gen, "localhost", 6002);
        //Client.createClient(0, gen, "localhost", 6003);
        //Client.createClient(0, gen, "localhost", 6004);
        //Client.createClient(0, gen, "localhost", 6005);
        //Client.createClient(0, gen, "localhost", 6006);
        //Client.createClient(0, gen, "localhost", 6007);
        //Client.createClient(0, gen, "localhost", 6008);
        //Client.createClient(0, gen, "localhost", 6009);
        //Client.createClient(0, gen, "localhost", 6010);
        //Client.createClient(0, gen, "localhost", 6011);

        //Client.createClient(1, gen, "localhost", 6001);
        //Client.createClient(2, gen, "localhost", 6002);
        //Client.createClient(3, gen, "localhost", 6003);
    }
}
