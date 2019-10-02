package com.ray.pbft.server;

import com.ray.mcu.server.Server;
import com.ray.mcu.utils.Log;
import com.ray.mcu.views.GlobalView;
import com.ray.pbft.communication.wrappers.PrePrepareWrapper;
import com.ray.pbft.communication.wrappers.PrepareWrapper;
import org.boon.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PbftServer extends Server
{
    /**
     * Contains current preprepare (Assumed for current view id)
     */
    public Pair<Integer, PrePrepareWrapper> currentPrePrepare = new Pair<>();

    /**
     * Contains all prepares (Assumed for view id + 1). todo right datatype
     */
    public Set<PrepareWrapper> prepareSet = new HashSet<>();

    /**
     * Contains all commits, store past commits to let others recover. todo right datatype
     */
    public HashMap<Integer, HashSet<PrePrepareWrapper>> commitMap = new HashMap<>();

    /**
     * Pending unregisters.
     */
    public Set<Integer> pendingUnregisters = new HashSet<>();

    /**
     * Create a server object.
     *
     * @param id   the server id.
     * @param ip   the server ip.
     * @param port the server port.
     */
    public PbftServer(final int id, final String ip, final int port)
    {
        super(id, ip, port);
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

        final PbftServer server = new PbftServer(id, ip, port);
        server.start();
    }
}
