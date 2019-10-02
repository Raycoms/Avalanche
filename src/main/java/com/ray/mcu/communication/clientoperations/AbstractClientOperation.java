package com.ray.mcu.communication.clientoperations;

import com.ray.mcu.communication.messages.IMessageWrapper;

import java.security.PublicKey;

/**
 * Client response operation type.
 */
public abstract class AbstractClientOperation implements IClientOperation
{
    /**
     * The message to be sent.
     */
    protected final IMessageWrapper message;

    /**
     * The id of the server to send it to.
     */
    protected final PublicKey id;

    /**
     * Create an instance of the client response operation.
     *
     * @param message the message to send.
     * @param id the client to send it to.
     */
    public AbstractClientOperation(final IMessageWrapper message, final PublicKey id)
    {
        this.message = message;
        this.id = id;
    }
}
