package com.ray.pbft.server;

import com.ray.mcu.communication.serveroperations.BroadcastOperation;
import com.ray.mcu.communication.wrappers.IMessageWrapper;
import com.ray.mcu.communication.wrappers.PersistClientMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.Server;
import com.ray.mcu.server.ServerData;
import com.ray.mcu.utils.Log;
import com.ray.mcu.utils.ValidationUtils;
import com.ray.pbft.PbftMessageHandlerRegistry;
import com.ray.pbft.communication.wrappers.CommitWrapper;
import com.ray.pbft.communication.wrappers.PrePrepareWrapper;
import com.ray.pbft.communication.wrappers.PrepareWrapper;
import com.ray.pbft.utils.PBFTState;
import org.boon.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class PbftServer extends Server
{
    /**
     * Contains current preprepare (Assumed for current view id)
     */
    public Pair<Integer, PrePrepareWrapper> currentPrePrepare = null;

    /**
     * Contains unverified preprepares. Always discard all older prepares.
     */
    public Map<Integer, PrePrepareWrapper> unverifiedPrePrepare = new HashMap<>();

    /**
     * Contains past preprepares //todo guava cache, delete after x
     */
    public Map<Integer, PrePrepareWrapper> pastPrePrepare = new HashMap<>();

    /**
     * Contains all prepares (Assumed for view id + 1).
     */
    public Set<PrepareWrapper> prepareSet = new HashSet<>();

    /**
     * The prepares which are still missing a preprepare.
     */
    public Map<Integer, List<PrepareWrapper>> unverifiedPrepareSet = new HashMap<>();

    /**
     * Contains all commits, store past commits to let others recover.
     */
    public Map<Integer, List<CommitWrapper>> commitMap = new HashMap<>();

    /**
     * Contains all univerified commits, store past commits to let others recover.
     */
    public Map<Integer, List<CommitWrapper>> unverifiedcommitMap = new HashMap<>();

    /**
     * Pending unregisters.
     */
    public Set<Integer> pendingUnregisters = new HashSet<>();

    /**
     * The current pbft state the replica is in.
     */
    public PBFTState status = PBFTState.NULL;

    /**
     * Commit counter to monitor performance.
     */
    public int counter = 0;

    /**
     * Cache which holds the messages to send in the future (Produced by Server).
     */
    public final LinkedBlockingQueue<PrePrepareWrapper> persistenceQueue = new LinkedBlockingQueue<>();

    /**
     * List of pending client messages to be proposed.
     */
    private List<MessageProto.PersistClientMessage> pendingClientLog = new ArrayList<>();

    /*
     * Loader for the Message handler.
     */
    static
    {
        // Load the PbtMessageHandlerRegistry.
        PbftMessageHandlerRegistry registry = new PbftMessageHandlerRegistry();
    }

    /**
     * Create a server object.
     *
     * @param id   the server id.
     * @param ip   the server ip.
     * @param port the server port.
     */
    public PbftServer(final int id, final String ip, final int port)
    {
        super(id, ip, port);
        new CommitValidator(this).start();
    }

    /**
     * Create a server object from the view.
     *
     * @param id   the server id.
     */
    public PbftServer(final int id)
    {
        super(id);
        new CommitValidator(this).start();
    }

    /**
     * The commit validator thread.
     */
    public class CommitValidator extends Thread
    {
        /**
         * The server this belongs to.
         */
        public PbftServer server;

        /**
         * Constructor for the validator to set the server object.
         * @param server the server to set.
         */
        public CommitValidator(final PbftServer server)
        {
            this.server = server;
        }

        @Override
        public void run()
        {
            while ( true )
            {
                try
                {
                    final PrePrepareWrapper prep = server.persistenceQueue.take();

                    // Verify is message log is valid.
                    if (!ValidationUtils.isMessageLogValid(prep.message.getPrePrepare(), server))
                    {
                        return;
                    }

                    prep.getMessage().getPrePrepare().getInputList().forEach(
                      m -> {
                          server.persist(m.getMsg());
                          server.counter++;
                      });

                    Log.getLogger().warn(server.counter);
                    //state.forEach((key, value) -> Log.getLogger().warn("New State for client: " + key.getEncoded()[0] + ": " + value));
                }
                catch (InterruptedException e)
                {
                    // Waking up.
                }
            }
        }
    }
    /**
     * Persist the current consensus result.
     * @param prep the prepare to commit.
     */
    public void persistConsensusResult(final PrePrepareWrapper prep)
    {
       persistenceQueue.add(prep);
    }

    /**
     * Start an instance of a server
     * @param args the arguments of the server (id, ip, host)
     */
    public static void main(final String[] args)
    {
        if (args.length == 1)
        {
            final int id = Integer.parseInt(args[0]);
            final PbftServer server = new PbftServer(id);
            server.start();
            return;
        }

        if (args.length < 3)
        {
            Log.getLogger().warn("Invalid arguments, at least 3 necessary!");
            return;
        }

        final int id = Integer.parseInt(args[0]);
        final String ip = args[1];
        final int port = Integer.parseInt(args[2]);

        final PbftServer server = new PbftServer(id, ip, port);
        server.start();
    }

    /**
     * Update the state of the current server.
     */
    public void updateState()
    {
        if (this.status == PBFTState.NULL)
        {
            this.status = PBFTState.PREPARE;
        }
        final int msgViewId = currentPrePrepare.getFirst();

        // Check if we have univerified prepares.
        if (!this.unverifiedPrepareSet.getOrDefault(msgViewId, new ArrayList<>()).isEmpty())
        {
            this.prepareSet.addAll(this.unverifiedPrepareSet.getOrDefault(msgViewId, new ArrayList<>()).stream().filter(this::validatePrepare).collect(Collectors.toList()));
            this.unverifiedPrepareSet.remove(msgViewId);
        }

        // Check if we have enough verified prepares to advance state.
        if (this.prepareSet.size() >= this.view.getQuorumSize() && this.status == PBFTState.PREPARE)
        {
            //Log.getLogger().warn("Broadcasting commit on: " + this.getServerData().getId() + " at view: " + this.view.getId());
            this.outputQueue.add(new BroadcastOperation(CommitWrapper.createCommitWrapper(this, this.prepareSet.toArray(new PrepareWrapper[0]))));
            this.status = PBFTState.COMMIT;
        }

        // Check if we have unverified commits.
        if (!this.unverifiedcommitMap.getOrDefault(msgViewId, new ArrayList<>()).isEmpty())
        {
            final List<CommitWrapper> list = this.unverifiedcommitMap.remove(msgViewId);
            list.removeIf(item -> !validateCommit(item));

            this.commitMap.put(msgViewId, list);
        }

        // Check if we have enough verified commits to advance state.
        if (this.commitMap.getOrDefault(msgViewId, new ArrayList<>()).size() >= this.view.getQuorumSize())
        {
            this.getView().updateView(this.currentPrePrepare.getSecond().getMessage().getPrePrepare().getView(), this);
            this.pastPrePrepare.put(currentPrePrepare.getFirst(), currentPrePrepare.getSecond());
            final Pair<Integer, PrePrepareWrapper> prep = this.currentPrePrepare;
            this.persistConsensusResult(prep.getSecond());
            this.currentPrePrepare = null;
            this.prepareSet.clear();
            this.status = PBFTState.NULL;
        }
    }

    /**
     * Validate if the commit message is valid (hash matches preprepare)
     * @param message the message to check.
     * @return true if so.
     */
    public boolean validateCommit(final CommitWrapper message)
    {
        // Check if commit hash matches preprepare hash.
        if (!Arrays.equals(message.getMessage().getCommit().getInputHash().toByteArray(), this.currentPrePrepare.getSecond().message.getSig().toByteArray()))
        {
            Log.getLogger().warn("----------------------------------------------------------------\n"
                                   + "Commit doesn't match Preprepare! (" + message.getSender() + ")"
                                   + "\n----------------------------------------------------------------");
            return false;
        }

        // Check greedy if commit has enough signatures at all.
        if (message.getMessage().getCommit().getSignaturesCount() < this.view.getQuorumSize())
        {
            Log.getLogger().warn("----------------------------------------------------------------\n"
                                   + "Commit doesn't have enough signatures! (" + message.getSender() + ")"
                                   + "\n----------------------------------------------------------------");
            return false;
        }

        // Check if commit has enough valid signatures in general.
        if (!ValidationUtils.verifyCommit(message.getMessage().getCommit().getSignaturesList(), this))
        {
            Log.getLogger().warn("----------------------------------------------------------------\n"
                                   + "Commit doesn't have enough valid signatures! (" + message.getSender() + ")"
                                   + "\n----------------------------------------------------------------");
            return false;
        }

        return true;
    }

    /**
     * Validate if the prepare message is valid (hash matches preprepare)
     * @param message the message to check.
     * @return true if so.
     */
    public boolean validatePrepare(final IMessageWrapper message)
    {
        if (!Arrays.equals(message.getMessage().getPrepare().getInputHash().toByteArray(), this.currentPrePrepare.getSecond().message.getSig().toByteArray()))
        {
            Log.getLogger().warn("----------------------------------------------------------------\n"
                                   + "Prepare doesn't match Preprepare! (" + message.getSender() + ")"
                                   + "\n----------------------------------------------------------------");
            return false;
        }

        return true;
    }

    /**
     * Get the PrePrepare for this ID.
     * @param id the view id of the prepare.
     * @return the correct PrePrepare wrapper or null.
     */
    @Nullable
    public PrePrepareWrapper getPrePrepareForId(final int id)
    {
        if (currentPrePrepare != null && currentPrePrepare.getFirst() == id)
        {
            return currentPrePrepare.getSecond();
        }
        return pastPrePrepare.getOrDefault(id, null);
    }

    @Override
    public void handleClientMessage(final MessageProto.Message message)
    {
        pendingClientLog.add(new PersistClientMessageWrapper(this, message.getClientMsg(), message.getSig()).message.getPersClientMsg());
        if ( pendingClientLog.size() > 200 && getView().getCoordinator() == getServerData().getId() && status == PBFTState.NULL)
        {
            Log.getLogger().warn(pendingClientLog.size());
            this.outputQueue.add(new BroadcastOperation(PrePrepareWrapper.createPrePrepareWrapper(this, pendingClientLog)));
            pendingClientLog.clear();
            status = PBFTState.PREPARE;
        }
    }

    @Override
    public void unregister(final ServerData data)
    {
        pendingUnregisters.add(data.getId());
    }
}
