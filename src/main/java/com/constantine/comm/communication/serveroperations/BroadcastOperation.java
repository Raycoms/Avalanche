package com.constantine.comm.communication.serveroperations;

import com.constantine.comm.communication.ISender;
import com.constantine.comm.communication.messages.IMessageWrapper;

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
