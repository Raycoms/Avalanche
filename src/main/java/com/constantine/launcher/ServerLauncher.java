package com.constantine.launcher;

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
        final GlobalView view = ViewLoader.loadView(CONFIG_LOCATION + "view.json");
        KeyUtilities.generateOrLoadKeys(view.getServers(), CONFIG_LOCATION);

        for (final ServerData server : view.getServers())
        {
            final Server thread = new Server(server.getId(), server.getIp(), server.getPort());
            thread.start();
        }
    }
}
