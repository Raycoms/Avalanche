package com.ray.mcu.client;

import com.ray.mcu.utils.KeyUtilities;

import java.security.PublicKey;
import java.util.Objects;

/**
 * ServerReceiver representation in View.
 */
public class ClientData
{
    /**
     * The unique client id.
     */
    private PublicKey id;

    /**
     * The client ip.
     */
    private String ip;

    /**
     * The client port.
     */
    private int port;


    /**
     * Create a client object.
     *
     * @param id   the client id.
     * @param ip   the client ip.
     * @param port the client port.
     */
    public ClientData(final PublicKey id, final String ip, final int port)
    {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    /**
     * Get the server id.
     *
     * @return the int id.
     */
    public PublicKey getId()
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
     * Verify if this server signed a handlers.
     * @param message the handlers.
     * @param signature the signature.
     * @return true if valid.
     */
    public boolean verifyKey(final byte[] message, final byte[] signature)
    {
        return KeyUtilities.verifyKey(message, signature, id);
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
        final ClientData that = (ClientData) o;
        return id.equals(that.id) &&
                 port == that.port &&
                 ip.equals(that.ip);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, ip, port);
    }
}
