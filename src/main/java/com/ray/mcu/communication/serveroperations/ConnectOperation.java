package com.ray.mcu.communication.serveroperations;

import com.ray.mcu.communication.ISender;
import com.ray.mcu.server.ServerData;

/**
 * The Connect operation to connect to a new server.
 */
public class ConnectOperation implements IOperation
{
    /**
     * The server to connect to.
     */
    private final ServerData server;

    /**
     * Create an instance of the connect operation.
     * @param server the server to connect to.
     */
    public ConnectOperation(final ServerData server)
    {
        this.server = server;
    }

    @Override
    public void executeOP(final ISender sender)
    {
        sender.connectToServer(this.server);
    }
}
