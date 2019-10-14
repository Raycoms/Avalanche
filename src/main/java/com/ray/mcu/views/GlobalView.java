package com.ray.mcu.views;

import com.ray.mcu.communication.serveroperations.DisconnectOperation;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.Server;
import com.ray.mcu.server.ServerData;
import com.ray.mcu.utils.Log;
import org.boon.json.annotations.JsonIgnore;
import org.boon.json.annotations.JsonInclude;

import java.util.*;
import java.util.stream.Collectors;

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
     * Get the quorum size for this view.
     * @return the quorum size.
     */
    public int getQuorumSize()
    {
        return ( ( getServers().size() / 3 ) * 2 ) + 1;
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
     * @param pendingUnregisters
     * @return null if invalid, else a list with the removed replicas.
     */
    public boolean validateView(final MessageProto.View view, final Set<Integer> pendingUnregisters)
    {
        if (view.getId() < this.getId() || view.getCoordinator() != this.getCoordinator())
        {
            Log.getLogger().error("View Id or Coordinator don't match!");
            return false;
        }

        for (final MessageProto.Server server : view.getServersList())
        {
            if (!servers.containsKey(server.getId()) && !pendingUnregisters.contains(server.getId()))
            {
                Log.getLogger().error("View is missing replica which is not pending to be unregistered");
                return false;
            }

            final ServerData existing = servers.get(server.getId());

            if (!server.getIp().equals(existing.getIp()) || server.getPort() != existing.getPort())
            {
                Log.getLogger().error("Servers in few with same id got different access parameters!");
                return false;
            }
        }

        return true;
    }

    /**
     * Update the view regarding the view of the pre-prepare.
     * @param view the incoming view.
     * @param server the server updating the view.
     */
    public void updateView(final MessageProto.View view, final Server server)
    {
        final List<ServerData> list = this.getServers().stream().filter(v -> view.getServersList().stream().noneMatch(s -> s.getId() == v.getId())).collect(Collectors.toList());
        for (final ServerData remove : list)
        {
            this.removeServer(remove.getId());
            server.outputQueue.add(new DisconnectOperation(remove));
        }

        view.getServersList().stream().filter(s -> this.getServers().stream().noneMatch(v -> s.getId() == v.getId())).forEach(s -> addServer(new ServerData(s.getId(), s.getIp(), s.getPort())));
        this.id++;
        // Here we adjust the coordinator in the future too.
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

        for (final ServerData serverData : this.getServers())
        {
            viewBuilder.addServers(MessageProto.Server.newBuilder().setId(serverData.getId()).setPort(serverData.getPort()).setIp(serverData.getIp()));
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
