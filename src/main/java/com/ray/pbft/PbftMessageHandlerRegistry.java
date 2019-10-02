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
import com.ray.pbft.communication.wrappers.PrePrepareWrapper;
import com.ray.pbft.communication.wrappers.PrepareWrapper;
import com.ray.pbft.server.PbftServer;
import io.netty.channel.ChannelHandlerContext;
import org.boon.Pair;
import sun.security.rsa.RSAPublicKeyImpl;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

/**
 * The Pbft specific message handler registry.
 */
public class PbftMessageHandlerRegistry
{
    /*
     * General todo list
     *
     * todo Add prepare
     * todo Add commit
     *
     * Add recover current consensus data (case of lost preprepare)
     * Add recover past consensus results
     */

    /*
     * Register additional messages here.
     */
    static
    {
        MessageHandlerRegistry.handlers.add(new PrePrepareMessageHandler());
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
            server.inputQueue.add(new PrePrepareWrapper(sender, message));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            if (!(server instanceof PbftServer))
            {
                Log.getLogger().warn("Turn on a PBFT Server to validate this message");
                return;
            }

            if (server.getView().getCoordinator() != message.getSender())
            {
                Log.getLogger().warn("----------------------------------------------------------------\n"
                                       + "Received Preprepare from other than the current Coordinator! (" + message.getSender() + ")"
                                       + "\n----------------------------------------------------------------");
                return;
            }

            final PbftServer pbftServer = (PbftServer) server;
            if (pbftServer.currentPrePrepare.getFirst() == pbftServer.getView().getId())
            {
                Log.getLogger().warn("----------------------------------------------------------------\n"
                                       + "Already received preprepare for this view id! (" + message.getSender() + ")"
                                       + "\n----------------------------------------------------------------");
                return;
            }

            final List<Integer> difference = pbftServer.getView().validateView(message.getMessage().getPrePrepare().getView());
            if (difference == null)
            {
                if (pbftServer.getView().getId() < message.getMessage().getPrePrepare().getView().getId())
                {
                    server.outputQueue.add(new UnicastOperation());
                    //todo request recovery
                    //todo should we continue normally after that?
                }
                else
                {
                    Log.getLogger().warn("----------------------------------------------------------------\n"
                                           + "Couldn't validate the view, invalid parameters! (" + message.getSender() + ")"
                                           + "\n----------------------------------------------------------------");
                }
                return;
            }
            else if (!difference.isEmpty())
            {
                for (final Integer s : difference)
                {
                    if (!pbftServer.pendingUnregisters.contains(s))
                    {
                        Log.getLogger().warn("----------------------------------------------------------------\n"
                                               + "View was missing a replica which didn't request to leave! (" + message.getSender() + ")"
                                               + "\n----------------------------------------------------------------");
                    }
                }
            }

            for (final MessageProto.PersistClientMessage data : message.getMessage().getPrePrepare().getInputList())
            {
                final MessageProto.ClientMessage msg = data.getMsg();

                try
                {
                    final PublicKey key = new RSAPublicKeyImpl(msg.getPkey().toByteArray());
                    if (!KeyUtilities.verifyKey(msg.toByteArray(), message.getMessage().getPersClientMsg().getSig().toByteArray(),key ))
                    {
                        Log.getLogger().warn("Invalid signature from client!");
                        return;
                    }

                    int tempState = server.state.getOrDefault(key, 0);

                    if (tempState + msg.getDif() < 0)
                    {
                        Log.getLogger().warn("----------------------------------------------------------------\n"
                                               + "Transactions tried to debit invalid quantity! (" + message.getSender() + ")"
                                               + "\n----------------------------------------------------------------");
                        return;
                    }

                    tempState += msg.getDif();

                    Log.getLogger().warn("New State: " + tempState);
                    server.state.put(key, tempState);
                }
                catch (InvalidKeyException e)
                {
                    Log.getLogger().warn("----------------------------------------------------------------\n"
                                           + "Transactions included invalidly signed client transaction! (" + message.getSender() + ")"
                                           + "\n----------------------------------------------------------------");
                    return;
                }
            }

            pbftServer.currentPrePrepare = new Pair<>(pbftServer.getView().getId(), (PrePrepareWrapper) message);

            server.outputQueue.add(new BroadcastOperation(new PrepareWrapper(server, ((PrePrepareWrapper) message).message)));
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
            server.inputQueue.add(new PrepareWrapper(sender, message));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            if (!(server instanceof PbftServer))
            {
                Log.getLogger().warn("Turn on a PBFT Server to validate this message");
                return;
            }

            if (server.getView().getId() > message.getMessage().getPrepare().getView().getId())
            {
                Log.getLogger().warn("Old Prepare - Discarding");
                return;
            }

            final PbftServer pbftServer = (PbftServer) server;

            //       View id > currentview (request dif and save to unverified)
            //       Didn't get pre-prepare yet (request and save to univerified)
            if (pbftServer.currentPrePrepare == null || pbftServer.currentPrePrepare.getFirst() < message.getMessage().getPrepare().getView().getId())
            {
                server.outputQueue.add(new UnicastOperation());
                //todo request recovery
                //todo should we continue normally after that?
                return;
            }

            if (!Arrays.equals(message.getMessage().getPrepare().getInputHash().toByteArray(), pbftServer.currentPrePrepare.getSecond().message.getSig().toByteArray())
            {
                Log.getLogger().warn("----------------------------------------------------------------\n"
                                       + "Prepare doesn't match Preprepare! (" + message.getSender() + ")"
                                       + "\n----------------------------------------------------------------");
                return;
            }

            if (pbftServer.prepareSet.size() + 1 > (pbftServer.view.getServers().size() / ( 3 * 2 ) ) + 1)
            {
                server.outputQueue.add(new BroadcastOperation());
                //todo commit message
            }
            else
            {
                pbftServer.prepareSet.add((PrepareWrapper) message);
            }
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
}
