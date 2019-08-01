package com.constantine.server;

import com.constantine.communication.messages.IMessageWrapper;
import com.constantine.communication.operations.IOperation;

/**
 * Server interface.
 */
public interface IServer
{
    /**
     * Serialized message handler (No concurrent operations).
     * In the future we might want a middleware ordering requests with ids.
     * @param message the incoming message to handle.
     */
    public void handleMessage(final IMessageWrapper message);

    /**
     * Getter for the ServerData,
     * @return the ServerData.
     */
    public ServerData getServerData();

    /**
     * Sign a handlers with the private key of this server.
     * @param message the handlers to sign.
     * @return the resulting signature.
     */
    public byte[] signMessage(final byte[] message);

    /**
     * Check if the server has a message to be sent.
     * @return true if so.
     */
    public boolean hasMessageInOutputQueue();

    /**
     * Consume a messgage from the output queue.
     * @return the message.
     */
    public IOperation consumeMessageFromOutputQueue();
}
