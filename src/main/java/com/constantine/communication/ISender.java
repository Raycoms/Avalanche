package com.constantine.communication;

import com.constantine.server.ServerData;

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
     * Start an unicast to a server.
     * @param message the message to send.
     * @param id the id of the server.
     */
    public void unicast(final String message, final int id);

    /**
     * Start a multicast to a list of servers.
     * @param message the message to send.
     * @param list the list of servers.
     */
    public void multicast(final String message, final int...list);

    /**
     * Start a broadcast to all known servers.
     * @param message the message to broadcast.
     */
    public void broadcast(final String message);
}
