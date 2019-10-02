package com.ray.mcu.server.client;

import com.ray.mcu.server.Server;

/**
 * Thread handling the client Messages.
 */
public class ClientMessageHandler extends Thread
{
    /**
     * The Server object this runs on.
     */
    private final Server server;

    /**
     * Create the Client message handler.
     * @param server the server it belongs to.
     */
    public ClientMessageHandler(final Server server)
    {
        this.server = server;
    }

    @Override
    public void run()
    {
        while (server.isActive())
        {
            if (server.clientInputQueue.isEmpty())
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
            server.handleMessage(server.clientInputQueue.poll());
        }
    }
}
