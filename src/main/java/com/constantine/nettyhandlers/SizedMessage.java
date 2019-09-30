package com.constantine.nettyhandlers;

/**
 * A generic handlers with a specific size and its buffer.
 */
public class SizedMessage
{
    /**
     * The flag of this message.
     */
    public static final byte FLAG = 0;

    /**
     * The size of the handlers.
     */
    public int id;

    /**
     * The handlers data.
     */
    public byte[] buffer;

    /**
     * Constructor of the sized handlers.
     * @param buffer the bytebuffer to send.
     * @param id the id of the sender.
     */
    public SizedMessage(final byte[] buffer, final int id)
    {
        this.buffer = buffer;
        this.id = id;
    }
}
