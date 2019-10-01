package com.constantine.comm.communication.clientoperations;

import com.constantine.comm.client.ClientData;
import com.constantine.comm.server.client.ServerClientSender;

/**
 * Client connect operation type.
 */
public class ClientConnectOperation
{
    /**
     * The clientdata to connect to.
     */
    private final ClientData data;

    /**
     * Create an instance of the client connect operation.
     *
     * @param data the data to connect to..
     */
    public ClientConnectOperation(final ClientData data)
    {
        this.data = data;
    }

    /**
     * Send the message.
     * @param sender the sender to send it to.
     */
    public void execute(final ServerClientSender sender)
    {
        sender.connectToClient(this.data);
    }
}
