package com.ray.mcu.server;

import com.ray.mcu.communication.clientoperations.IClientOperation;
import com.ray.mcu.communication.messages.IMessageWrapper;
import com.ray.mcu.communication.serveroperations.IOperation;

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

    /**
     * Consume a messgage from the output queue.
     * @return the message.
     */
    public IClientOperation consumeMessageFromClientOutputQueue();

    /**
     * Check if the server is still active or shut down already.
     * @return true as long as active.
     */
    boolean isActive();

    /**
     * Check if there is a message in the client ouput queue.
     * @return true if so.
     */
    boolean hasMessageInClientOutputQueue();
}
