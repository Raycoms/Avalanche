package com.ray.mcu.communication.clientoperations;

import com.ray.mcu.server.client.ServerClientSender;

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
