package com.constantine.communication.message;

/**
 * A generic message with a specific size and its buffer.
 */
public class SizedMessage
{
    /**
     * The size of the message.
     */
    public int size;

    /**
     * The message data.
     */
    public byte[] buffer;

    /**
     * Constructor of the sized message.
     * @param buffer the bytebuffer to send.
     */
    public SizedMessage(final byte[] buffer)
    {
        this.size = buffer.length;
        this.buffer = buffer;
    }
}
