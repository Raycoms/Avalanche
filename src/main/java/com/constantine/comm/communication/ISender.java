package com.constantine.comm.communication;

import com.constantine.comm.communication.messages.IMessageWrapper;
import com.constantine.comm.server.ServerData;

import java.util.List;

/**
 * Message sender interface.
 */
public interface ISender
{
    /**
     * Method to start up all the client handlers to the servers.
     */
    public void startUp();

    /**
     * Connect to an additional server.
     * @param data the server data to connect to.
     */
    public void connectToServer(final ServerData data);

    /**
     * Disconnect an existing server.
     * @param server the server to disconnect from.
     */
    void disconnectFromServer(ServerData server);

    /**
     * Start an unicast to a server.
     * @param message the message to send.
     * @param id the id of the server.
     */
    public void unicast(final IMessageWrapper message, final int id);

    /**
     * Start a multicast to a list of servers.
     * @param message the message to send.
     * @param list the list of servers.
     */
    public void multicast(final IMessageWrapper message, final List<Integer> list);

    /**
     * Start a broadcast to all known servers.
     * @param message the message to broadcast.
     */
    public void broadcast(final IMessageWrapper message);
}
