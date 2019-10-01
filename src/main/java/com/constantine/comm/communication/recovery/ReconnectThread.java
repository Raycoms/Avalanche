package com.constantine.comm.communication.recovery;

import com.constantine.comm.server.server.ServerNettySenderHandler;
import com.constantine.comm.server.ServerData;
import com.constantine.comm.utils.Log;
import io.netty.bootstrap.Bootstrap;

/**
 * Thread to reconnect to the sender.
 */
public class ReconnectThread extends Thread
{
    /**
     * The sender handler.
     */
    private final ServerNettySenderHandler handler;

    /**
     * The bootstrap instance to connect to.
     */
    private final Bootstrap b;

    /**
     * Constructor for the thread.
     * @param b the bootstrap instance.
     * @param handler the sender handler.
     */
    public ReconnectThread(final ServerNettySenderHandler handler, final Bootstrap b)
    {
        this.handler = handler;
        this.b = b;
    }

    @Override
    public void run()
    {
        super.run();

        int attempts = 0;
        //todo we probably want this timeout value to be configurable
        while (!handler.isActive() && attempts < 100)
        {
            if (!handler.isReconnecting())
            {
                handler.setReconnecting(true);
                Log.getLogger().warn("Trying to reconnect");

                final ServerData data = handler.getServerData();
                b.connect(data.getIp(), data.getPort());
                attempts++;
            }

            try
            {
                //todo we would also want a config value on this.
                Thread.sleep(250);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            if (!handler.isActive())
            {
                handler.setReconnecting(false);
            }
        }
    }
}