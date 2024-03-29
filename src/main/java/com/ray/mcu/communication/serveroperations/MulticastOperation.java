package com.ray.mcu.communication.serveroperations;

import com.ray.mcu.communication.ISender;
import com.ray.mcu.communication.wrappers.IMessageWrapper;

import java.util.List;

/**
 * Multicast operation type.
 */
public class MulticastOperation extends AbstractSendOperation
{
    /**
     * The ids of the server to send it to.
     */
    private final List<Integer> ids;

    /**
     * Create an instance of the multicast operation.
     *
     * @param message the message to send.
     * @param id the servers sto send it to.
     */
    public MulticastOperation(final IMessageWrapper message, final List<Integer> id)
    {
        super(message);
        this.ids = id;
    }

    @Override
    protected void send(final IMessageWrapper message, final ISender sender)
    {
        sender.multicast(message, this.ids);
    }
}
