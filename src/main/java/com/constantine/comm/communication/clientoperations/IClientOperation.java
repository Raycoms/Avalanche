package com.constantine.comm.communication.clientoperations;

import com.constantine.comm.server.client.ServerClientSender;

/**
 * Client response operation type.
 */
public interface IClientOperation
{
    /**
     * Send the message.
     * @param sender the sender to send it to.
     */
    void execute(final ServerClientSender sender);
}
