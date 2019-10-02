package com.ray.mcu.communication.serveroperations;

import com.ray.mcu.communication.ISender;
import com.ray.mcu.communication.messages.IMessageWrapper;

/**
 * Generic Send Operation.
 */
public abstract class AbstractSendOperation implements IOperation
{
    /**
     * The message to be sent.
     */
    private final IMessageWrapper message;

    /**
     * Create an instance of the send operation.
     * @param message the message to send.
     */
    public AbstractSendOperation(final IMessageWrapper message)
    {
        this.message = message;
    }

    @Override
    public void executeOP(final ISender sender)
    {
        send(message, sender);
    }

    /**
     * The actual send operation. Each class overrides this as fitting.
     * @param message the message to send.
     * @param sender the sender to use.
     */
    protected abstract void send(final IMessageWrapper message, final ISender sender);
}
