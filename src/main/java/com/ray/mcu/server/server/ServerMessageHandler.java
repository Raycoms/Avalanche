package com.ray.mcu.server.server;

import com.ray.mcu.communication.messages.TextMessageWrapper;
import com.ray.mcu.communication.messages.UnregisterRequestMessageWrapper;
import com.ray.mcu.communication.serveroperations.BroadcastOperation;
import com.ray.mcu.communication.serveroperations.UnicastOperation;
import com.ray.mcu.server.Server;

/**
 * Thread handling the Server Messages.
 */
public class ServerMessageHandler extends Thread
{
    /**
     * The Server object this runs on.
     */
    private final Server server;

    /**
     * Create the Server message handler.
     * @param server the server it belongs to.
     */
    public ServerMessageHandler(final Server server)
    {
        this.server = server;
    }

    @Override
    public void run()
    {
        int counter = 0;
        while (server.isActive())
        {
            //todo also remove in future
            if (++counter%40==0)
            {
                server.outputQueue.add(new BroadcastOperation(new TextMessageWrapper(server, "Heartbeat")));
            }

            if (counter%4000==0)
            {
                if ( server.getServerData().getId() == 4)
                {
                    server.outputQueue.add(new UnicastOperation(new UnregisterRequestMessageWrapper(server, server.getServerData()), server.view.getCoordinator()));
                }
                counter = 1;
            }

            if (server.inputQueue.isEmpty())
            {
                try
                {
                    //todo config value on this too
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                continue;
            }
            server.handleMessage(server.inputQueue.poll());
        }
    }
}
