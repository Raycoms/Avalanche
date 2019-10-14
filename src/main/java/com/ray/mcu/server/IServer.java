package com.ray.mcu.server;

import com.ray.mcu.communication.clientoperations.IClientOperation;
import com.ray.mcu.communication.wrappers.IMessageWrapper;
import com.ray.mcu.communication.serveroperations.IOperation;
import com.ray.mcu.views.GlobalView;

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
     * Consume a messgage from the output queue.
     * @return the message.
     */
    public IOperation consumeMessageFromOutputQueue() throws InterruptedException;

    /**
     * Consume a messgage from the output queue.
     * @return the message.
     */
    public IClientOperation consumeMessageFromClientOutputQueue() throws InterruptedException;

    /**
     * Check if the server is still active or shut down already.
     * @return true as long as active.
     */
    boolean isActive();

    /**
     * Get the view from the server.
     * @return the view.
     */
    GlobalView getView();
}
