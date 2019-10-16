package com.ray.pbft;

import com.google.protobuf.GeneratedMessageV3;
import com.ray.mcu.communication.IMessageHandler;
import com.ray.mcu.communication.MessageHandlerRegistry;
import com.ray.mcu.communication.serveroperations.BroadcastOperation;
import com.ray.mcu.communication.serveroperations.UnicastOperation;
import com.ray.mcu.communication.wrappers.IMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.Server;
import com.ray.mcu.utils.KeyUtilities;
import com.ray.mcu.utils.Log;
import com.ray.mcu.utils.ValidationUtils;
import com.ray.pbft.communication.wrappers.*;
import com.ray.pbft.server.PbftServer;
import com.ray.pbft.utils.PBFTState;
import io.netty.channel.ChannelHandlerContext;
import org.boon.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The Pbft specific message handler registry.
 */
public class PbftMessageHandlerRegistry
{
    /*
     * Register additional messages here.
     */
    static
    {
        MessageHandlerRegistry.handlers.add(new PrePrepareMessageHandler());
        MessageHandlerRegistry.handlers.add(new PrepareMessageHandler());
        MessageHandlerRegistry.handlers.add(new CommitMessageHandler());
        MessageHandlerRegistry.handlers.add(new RequestRecoverPrePrepareMessageHandler());
        MessageHandlerRegistry.handlers.add(new RequestRecoverCommitMessageHandler());
        MessageHandlerRegistry.handlers.add(new RecoverCommitMessageHandler());
    }

    /**
     * The classical handler of the preprepare message.
     *
     * Validates view and client requests.
     *
     * If valid results in broadcasting prepare message.
     */
    private static class PrePrepareMessageHandler implements IMessageHandler
    {

        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            server.addToInputQueue(new PrePrepareWrapper(sender, message.toBuilder()));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            //Log.getLogger().warn("Received PrePrepare on: " + server.getServerData().getId());

            if (!(server instanceof PbftServer))
            {
                Log.getLogger().warn("Turn on a PBFT Server to validate this message");
                return;
            }

            final PbftServer pbftServer = (PbftServer) server;
            final int msgViewId = message.getMessage().getPrePrepare().getView().getId();

            // Old preprepare
            if (pbftServer.currentPrePrepare != null && pbftServer.currentPrePrepare.getFirst() >= msgViewId)
            {
                Log.getLogger().warn("----------------------------------------------------------------\n"
                                       + server.getServerData().getId() + " Already received preprepare for this view id! (" + message.getSender() + ")"
                                       + "\n----------------------------------------------------------------");
                return;
            }

            // Supposedly newer pre-prepare
            if (pbftServer.getView().getId() < msgViewId)
            {
                ((PbftServer) server).unverifiedPrePrepare.put(msgViewId, (PrePrepareWrapper) message);
                server.addToOutputQueue(new UnicastOperation(new RequestRecoverCommitWrapper(server, server.getView().getId()), message.getSender()));
                return;
            }

            // If sender is not coordinator of current view.
            if (server.getView().getCoordinator() != message.getSender())
            {
                Log.getLogger().warn("----------------------------------------------------------------\n"
                                       + "Received Preprepare from other than the current Coordinator! (" + message.getSender() + ")"
                                       + "\n----------------------------------------------------------------");
                return;
            }

            // Verify if view is valid.
            if (!pbftServer.getView().validateView(message.getMessage().getPrePrepare().getView(), pbftServer.pendingUnregisters))
            {
                return;
            }

            // Verify is message log is valid.
            if (!ValidationUtils.isMessageLogValid( message.getMessage().getPrePrepare(), pbftServer))
            {
                return;
            }

            pbftServer.currentPrePrepare = new Pair<>(pbftServer.getView().getId(), (PrePrepareWrapper) message);
            server.addToOutputQueue(new BroadcastOperation(new PrepareWrapper(server, ((PrePrepareWrapper) message).message.build())));
            pbftServer.updateState();
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasPrePrepare();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getPrePrepare();
        }
    }

    /**
     * The classical handler of the prepare message.
     *
     * Verifies if hash corresponds to prepare message.
     * Adds prepare to array, after 2f+1, broadcast commit.
     *
     * If valid results in broadcasting commit message.
     */
    private static class PrepareMessageHandler implements IMessageHandler
    {

        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            server.addToInputQueue(new PrepareWrapper(sender, message.toBuilder()));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            //Log.getLogger().warn("Received Prepare on: " + server.getServerData().getId());

            if (!(server instanceof PbftServer))
            {
                Log.getLogger().warn("Turn on a PBFT Server to validate this message");
                return;
            }

            if (server.getView().getId() > message.getMessage().getPrepare().getView().getId())
            {
                //Log.getLogger().warn("Old Prepare - Discarding");
                return;
            }

            final PbftServer pbftServer = (PbftServer) server;

            // too new prepare (missing only currentPrePrepare, missing more)
            final int incViewId = message.getMessage().getPrepare().getView().getId();

            // If we don't have a prepare at the moment.
            if ( pbftServer.currentPrePrepare == null )
            {
                final List<PrepareWrapper> list = pbftServer.unverifiedPrepareSet.getOrDefault(incViewId, new ArrayList<>());
                list.add((PrepareWrapper) message);
                pbftServer.unverifiedPrepareSet.put(incViewId, list);

                server.addToOutputQueue(new UnicastOperation(new RequestRecoverPrePrepareWrapper(server, incViewId), message.getMessage().getPrepare().getView().getCoordinator()));
                return;
            }

            // If we have a prepare which is older than the incoming view id.
            if (pbftServer.currentPrePrepare.getFirst() < incViewId)
            {
                final List<PrepareWrapper> list = pbftServer.unverifiedPrepareSet.getOrDefault(incViewId, new ArrayList<>());
                list.add((PrepareWrapper) message);
                pbftServer.unverifiedPrepareSet.put(incViewId, list);

                server.addToOutputQueue(new UnicastOperation(new RequestRecoverCommitWrapper(server, incViewId), message.getMessage().getPrepare().getView().getCoordinator()));
                return;
            }

            if (!pbftServer.validatePrepare(message))
            {
                return;
            }

            pbftServer.prepareSet.add((PrepareWrapper) message);
            pbftServer.updateState();
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasPrepare();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getPrepare();
        }
    }

    /**
     * The classical handler of the commit message.
     *
     * Verifies if enough signatures are in it.
     * Verifies if signatures are valid.
     * Verifies if decision is on the same.
     * Verify if view is the same as of the pre-prepare.
     *
     * If we receive a commit and we don't have the prepare neither preprepare -> recover preprepare and store commit.
     * As always, if we're behind in view, recover everything.
     *
     * If valid persist changes, and increment view (also add view changes).
     */
    private static class CommitMessageHandler implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            server.addToInputQueue(new CommitWrapper(sender, message.toBuilder()));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            //Log.getLogger().warn("Received Commit on: " + server.getServerData().getId() + " of " + message.getSender());

            if (!(server instanceof PbftServer))
            {
                Log.getLogger().warn("Turn on a PBFT Server to validate this message");
                return;
            }

            if (server.getView().getId() > message.getMessage().getCommit().getView().getId())
            {
                //Log.getLogger().warn("Old Commit - Discarding");
                return;
            }

            final PbftServer pbftServer = (PbftServer) server;
            final int incViewId = message.getMessage().getCommit().getView().getId();

            // Current preprepare missing -> recover
            if (pbftServer.currentPrePrepare == null)
            {
                final List<CommitWrapper> list = pbftServer.unverifiedcommitMap.getOrDefault(incViewId, new ArrayList<>());
                list.add((CommitWrapper) message);
                pbftServer.unverifiedcommitMap.put(incViewId, list);

                server.addToOutputQueue(new UnicastOperation(new RequestRecoverPrePrepareWrapper(server, incViewId), message.getMessage().getCommit().getView().getCoordinator()));
                return;
            }

            // Current preprepare outdated, recover past commits.
            if (pbftServer.currentPrePrepare.getFirst() < incViewId)
            {
                final List<CommitWrapper> list = pbftServer.unverifiedcommitMap.getOrDefault(incViewId, new ArrayList<>());
                list.add((CommitWrapper) message);
                pbftServer.unverifiedcommitMap.put(incViewId, list);

                server.addToOutputQueue(new UnicastOperation(new RequestRecoverCommitWrapper(server, incViewId), message.getSender()));
            }

            if (! pbftServer.validateCommit((CommitWrapper) message))
            {
                return;
            }

            final List<CommitWrapper> current = pbftServer.commitMap.getOrDefault(incViewId, new ArrayList<>());
            current.add((CommitWrapper) message);
            pbftServer.commitMap.put(incViewId, current);
            pbftServer.updateState();
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasCommit();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getCommit();
        }
    }

    /**
     * Handles requests of recoverying pre-prepare messages.
     */
    private static class RequestRecoverPrePrepareMessageHandler implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            server.addToInputQueue(new RequestRecoverPrePrepareWrapper(sender, message.toBuilder()));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            Log.getLogger().warn("Received RequestRecoverPrePrepare on: " + server.getServerData().getId());

            final int requestViewId = message.getMessage().getRequestRecoverPrePrepare().getViewId();
            if (( ( PbftServer ) server ).currentPrePrepare != null && server.getView().getId() == requestViewId)
            {
                server.addToOutputQueue(new UnicastOperation( new PrePrepareWrapper(( ( PbftServer ) server ).currentPrePrepare.getSecond().sender, ( ( PbftServer ) server ).currentPrePrepare.getSecond().getMessage().toBuilder()), message.getSender()));
            }
            else if (requestViewId > server.getView().getId())
            {
                Log.getLogger().warn("----------------------------------------------------------------\n"
                                       + "Received a request for a view id exceeding the current view! (" + message.getSender() + ") - discarding"
                                       + "\n----------------------------------------------------------------");
            }
            else if ( (( PbftServer ) server ).pastPrePrepare.containsKey(requestViewId))
            {
                server.addToOutputQueue(new UnicastOperation( new PrePrepareWrapper(( ( PbftServer ) server ).pastPrePrepare.get(requestViewId).sender, ( ( PbftServer ) server ).pastPrePrepare.get(requestViewId).getMessage().toBuilder()), message.getSender()));
            }
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasRequestRecoverPrePrepare();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getRequestRecoverPrePrepare();
        }
    }

    /**
     * Handles recovering past commit messages.
     */
    private static class RequestRecoverCommitMessageHandler implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            server.addToInputQueue(new RequestRecoverCommitWrapper(sender, message.toBuilder()));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            Log.getLogger().warn("Received RequestRecoverCommit on: " + server.getServerData().getId());

            if (!(server instanceof PbftServer))
            {
                Log.getLogger().warn("Turn on a PBFT Server to validate this message");
                return;
            }

            final PbftServer pbftServer = (PbftServer) server;

            final int requestViewId = message.getMessage().getRequestRecoverCommit().getViewId();
            if (requestViewId > pbftServer.view.getId())
            {
                Log.getLogger().warn("----------------------------------------------------------------\n"
                                       + "Received a request for a view id exceeding the current view! (" + message.getSender() + ") - discarding"
                                       + "\n----------------------------------------------------------------");
                return;
            }

            final List recoverCommits = pbftServer.commitMap.entrySet().stream().filter(e -> e.getKey() >= requestViewId)
                                          .map(Map.Entry::getValue).map(l -> l.get(0))
                                          .sorted(Comparator.comparingInt(e -> e.getMessage().getCommit().getView().getId()))
                                          .collect(Collectors.toList());
            if (!recoverCommits.isEmpty())
            {
                server.addToOutputQueue(new UnicastOperation(RecoverCommitWrapper.createCommitWrapper(pbftServer, recoverCommits), message.getSender()));
            }
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasRequestRecoverCommit();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getRequestRecoverCommit();
        }
    }

    /**
     * Sent to a replica requesting to recover a commit.
     */
    private static class RecoverCommitMessageHandler implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            server.addToInputQueue(new RecoverCommitWrapper(sender, message.toBuilder()));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            Log.getLogger().warn("Received RecoverCommit on: " + server.getServerData().getId());
            if (!(server instanceof PbftServer))
            {
                Log.getLogger().warn("Turn on a PBFT Server to validate this message");
                return;
            }

            final PbftServer pbftServer = (PbftServer) server;

            for (final MessageProto.CommitStorage storage : message.getMessage().getRecoverCommit().getCommitsList())
            {
                if (storage.getView().getId() != pbftServer.getView().getId())
                {
                    Log.getLogger().warn("First view doesn't correspond with necessary view");
                }

                if (!pbftServer.getView().validateView(storage.getView(), pbftServer.pendingUnregisters))
                {
                    return;
                }

                final PrePrepareWrapper wrapper = PrePrepareWrapper.createPrePrepareWrapper(storage.getView(), storage.getView().getCoordinator(), storage.getInputList(), storage.getInputHash());

                if (!KeyUtilities.verifyKey(wrapper.getMessage().getPrePrepare().toByteArray(), storage.getInputHash().toByteArray(), server.getView().getServer(server.getView().getCoordinator()).getPublicKey()))
                {
                    Log.getLogger().error("Invalid signature of coordinator in commit recovery message");
                    return;
                }

                // Verify is message log is valid.
                if (!ValidationUtils.isMessageLogValid(wrapper.getMessage().getPrePrepare(), pbftServer))
                {
                    return;
                }

                pbftServer.currentPrePrepare = new Pair<>(pbftServer.getView().getId(), PrePrepareWrapper.createPrePrepareWrapper(storage.getView(), 0, storage.getInputList(), storage.getInputHash()) );
                if (!ValidationUtils.verifyCommit(storage.getSignaturesList(), pbftServer))
                {
                    return;
                }

                pbftServer.getView().updateView(pbftServer.currentPrePrepare.getSecond().getMessage().getPrePrepare().getView(), server);
                pbftServer.persistConsensusResult(pbftServer.currentPrePrepare);

                pbftServer.currentPrePrepare = null;
                pbftServer.status = PBFTState.NULL;
            }
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasRecoverCommit();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getRecoverCommit();
        }
    }
}
