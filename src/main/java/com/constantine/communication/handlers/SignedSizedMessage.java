package com.constantine.communication.handlers;

/**
 * A generic handlers with a specific size, signature and its buffer.
 */
public class SignedSizedMessage extends SizedMessage
{
    /**
     * The flag of this message.
     */
    public static final byte FLAG = 1;

    /**
     * The handlers signature.
     */
    public byte[] sig;

    /**
     * Constructor of the sized handlers.
     * @param buffer the bytebuffer to send.
     * @param id the id of the sender.
     * @param sig the signature of the sender.
     */
    public SignedSizedMessage(final byte[] buffer, final int id, final byte[] sig)
    {
        super(buffer, id);
        this.sig = sig;
    }
}
