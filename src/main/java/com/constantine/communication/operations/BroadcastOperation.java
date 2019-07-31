package com.constantine.communication.operations;

import com.constantine.communication.ISender;
import com.constantine.communication.messages.IMessageWrapper;

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
