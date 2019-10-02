package com.ray.mcu.views;

import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.ServerData;
import com.ray.mcu.utils.Log;
import org.boon.json.annotations.JsonIgnore;
import org.boon.json.annotations.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A view of the global network where all correct servers ought to have an identical one.
 */
public class GlobalView
{
    /**
     * The id of the View.
     */
    private int id;

    /**
     * Map of replicas from id to the server object.
     */
    private Map<Integer, ServerData> servers;

    /**
     * Id of the coordinator replica.
     */
    @JsonInclude
    private int coordinator;

    /**
     * The config location.
     */
    @JsonIgnore
    private String configLocation = "";

    /**
     * Instantiate the global view.
     * @param id the view id.
     * @param coordinator the coordinator id.
     * @param servers the server array.
     */
    public GlobalView(final int id, final int coordinator, final Map<Integer, ServerData> servers)
    {
        this.id = id;
        this.coordinator = coordinator;
        this.servers = servers;
    }

    /**
     * Getter for the id of the view.
     * @return the id.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Getter for the coordinator.
     * @return the coordinator id.
     */
    public int getCoordinator()
    {
        return coordinator;
    }

    /**
     * Get the ServerReceiver instance for an id.
     * @param id the int id.
     * @return the ServerReceiver instance.
     */
    public ServerData getServer(final int id)
    {
        return servers.getOrDefault(id, null);
    }

    /**
     * Get the list of all servers.
     * @return a copy of the server list.
     */
    public List<ServerData> getServers()
    {
        return new ArrayList<>(servers.values());
    }

    /**
     * Increment the current view id.
     */
    public void incrementViewId()
    {
        this.id++;
    }

    /**
     * Set a new server to be coordinator.
     * @param coordinator the id of the new coordinator.
     */
    public void setCoordinator(final int coordinator)
    {
        this.coordinator = coordinator;
    }

    /**
     * Add a new server to the map of servers.
     * @param server the server to add.
     */
    public void addServer(final ServerData server)
    {
        server.loadPublicKey(this.configLocation);
        servers.put(server.getId(), server);
    }

    /**
     * Remove a server from the map.
     * @param id the id of the server to remove.
     */
    public void removeServer(final int id)
    {
        servers.remove(id);
    }

    /**
     * Set the location the view is at.
     * @param configLocation the relative location.
     * @return this view.
     */
    public GlobalView setConfigLocation(final String configLocation)
    {
        this.configLocation = configLocation;
        return this;
    }

    /**
     * Validate the existing view with an incoming view.
     * @param view the view to check.
     * @return null if invalid, else a list with the removed replicas.
     */
    public List<Integer> validateView(final MessageProto.View view)
    {
        if (view.getId() == this.getId() || view.getCoordinator() != this.getCoordinator())
        {
            Log.getLogger().warn("View Id or Coordinator don't match!");
            return null;
        }

        final List<Integer> difference = new ArrayList<>();
        for (final MessageProto.Server server : view.getServersList())
        {
            if (!servers.containsKey(server.getId()))
            {
                difference.add(server.getId());
                continue;
            }

            final ServerData existing = servers.get(server.getId());

            if (!server.getIp().equals(existing.getIp()) || server.getPort() != existing.getPort())
            {
                Log.getLogger().warn("Servers in few with same id got different access parameters!");
                return null;
            }
        }
        return difference;
    }

    /**
     * Process this existing view to its protobuf version for sending.
     * @return the built View.
     */
    public MessageProto.View processViewToProto()
    {
        final MessageProto.View.Builder viewBuilder = MessageProto.View.newBuilder();
        viewBuilder.setCoordinator(this.getCoordinator());
        viewBuilder.setId(this.getId());

        int index = 0;
        for (final ServerData serverData : this.getServers())
        {
            viewBuilder.setServers(index++, MessageProto.Server.newBuilder().setId(serverData.getId()).setPort(serverData.getPort()).setIp(serverData.getIp()));
        }

        return viewBuilder.build();
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
        final GlobalView that = (GlobalView) o;
        if (id != that.id || coordinator != that.coordinator)
        {
            return false;
        }

        if (that.servers.size() != servers.size())
        {
            return false;
        }

        for (final Map.Entry<Integer, ServerData> k : servers.entrySet())
        {
            if (!k.getValue().equals(that.servers.getOrDefault(k.getKey(), null)))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, servers, coordinator);
    }
}
