package com.constantine.server;

import com.constantine.utils.KeyUtilities;
import org.boon.json.annotations.JsonIgnore;
import org.boon.json.annotations.JsonInclude;

import java.security.*;
import java.util.Objects;

/**
 * ServerReceiver representation in View.
 */
public class ServerData
{
    /**
     * The unique server id.
     */
    @JsonInclude
    private int id;

    /**
     * The server ip.
     */
    private String ip;

    /**
     * The server port.
     */
    private int port;

    /**
     * The port the client connects to.
     */
    private int cport;

    @JsonIgnore
    private PublicKey publicKey;

    /**
     * Create a server object.
     *
     * @param id   the server id.
     * @param ip   the server ip.
     * @param port the server port.
     */
    public ServerData(final int id, final String ip, final int port)
    {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.cport = port - 1020;
    }

    /**
     * Create a server object.
     *
     * @param id   the server id.
     * @param ip   the server ip.
     * @param port the server port.
     * @param cport the client server port.
     */
    public ServerData(final int id, final String ip, final int port, final int cport)
    {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.cport = cport;
    }

    /**
     * Get the server id.
     *
     * @return the int id.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Get the server ip.
     *
     * @return the string of the ip.
     */
    public String getIp()
    {
        return ip;
    }

    /**
     * Get the server port.
     *
     * @return the int port.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Get the client server port.
     *
     * @return the int port.
     */
    public int getCport()
    {
        return cport;
    }

    /**
     * Set the public key of the server.
     *
     * @param publicKey the public key to set.
     */
    public void setPublicKey(final PublicKey publicKey)
    {
        this.publicKey = publicKey;
    }

    /**
     * Getter for the public key of the server.
     *
     * @return the public key instance.
     */
    public PublicKey getPublicKey()
    {
        return this.publicKey;
    }

    /**
     * Verify if this server signed a handlers.
     * @param message the handlers.
     * @param signature the signature.
     * @return true if valid.
     */
    public boolean verifyKey(final byte[] message, final byte[] signature)
    {
        return KeyUtilities.verifyKey(message, signature, publicKey);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final ServerData that = (ServerData) o;
        return id == that.id &&
                 port == that.port &&
                 ip.equals(that.ip);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, ip, port);
    }
}
