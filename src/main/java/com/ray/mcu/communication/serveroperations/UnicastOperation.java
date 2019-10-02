package com.ray.mcu.communication.serveroperations;

import com.ray.mcu.communication.ISender;
import com.ray.mcu.communication.messages.IMessageWrapper;

/**
 * Unicast operation type.
 */
public class UnicastOperation extends AbstractSendOperation
{
    /**
     * The id of the server to send it to.
     */
    private final int id;

    /**
     * Create an instance of the unicast operation.
     *
     * @param message the message to send.
     * @param id the server to send it to.
     */
    public UnicastOperation(final IMessageWrapper message, final int id)
    {
        super(message);
        this.id = id;
    }

    @Override
    protected void send(final IMessageWrapper message, final ISender sender)
    {
        sender.unicast(message, this.id);
    }
}
