package com.ray.mcu.communication.clientoperations;

import com.ray.mcu.communication.wrappers.IMessageWrapper;
import com.ray.mcu.server.client.ServerClientSender;

import java.security.PublicKey;

/**
 * Client response operation type.
 */
public class ClientResponseOperation extends AbstractClientOperation
{
    /**
     * Create an instance of the client response operation.
     *
     * @param message the message to send.
     * @param id the client to send it to.
     */
    public ClientResponseOperation(final IMessageWrapper message, final PublicKey id)
    {
        super(message, id);
    }

    /**
     * Send the message.
     * @param sender the sender to send it to.
     */
    public void execute(final ServerClientSender sender)
    {
        sender.send(this.message, this.id);
    }
}
