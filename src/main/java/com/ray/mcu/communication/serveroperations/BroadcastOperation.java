package com.ray.mcu.communication.serveroperations;

import com.ray.mcu.communication.ISender;
import com.ray.mcu.communication.wrappers.IMessageWrapper;

/**
 * Broadcast operation type.
 */
public class BroadcastOperation extends AbstractSendOperation
{
    /**
     * Create an instance of the broadcast operation.
     *
     * @param message the message to send.
     */
    public BroadcastOperation(final IMessageWrapper message)
    {
        super(message);
    }

    @Override
    protected void send(final IMessageWrapper message, final ISender sender)
    {
        sender.broadcast(message);
    }
}
