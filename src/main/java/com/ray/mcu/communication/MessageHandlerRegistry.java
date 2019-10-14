package com.ray.mcu.communication;

import com.ray.mcu.communication.wrappers.*;
import com.ray.mcu.communication.serveroperations.BroadcastOperation;
import com.ray.mcu.communication.serveroperations.ConnectOperation;
import com.ray.mcu.communication.serveroperations.UnicastOperation;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.Server;
import com.ray.mcu.server.ServerData;
import com.ray.mcu.utils.KeyUtilities;
import com.ray.mcu.utils.Log;
import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import sun.security.rsa.RSAPublicKeyImpl;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * The Message handler registry.
 */
public final class MessageHandlerRegistry
{
    /**
     * List of handlers to handle incoming messages.
     */
    public static final List<IMessageHandler> handlers = new ArrayList<>();

    static
    {
        handlers.add(new TextMessageHandler());
        handlers.add(new IntMessageHandler());
        handlers.add(new RegisterRequestMessage());
        handlers.add(new RegisterMessage());
        handlers.add(new UnregisterMessage());
        handlers.add(new UnregisterRequestMessage());
        handlers.add(new ClientMessage());
        handlers.add(new PersistClientMessage());
    }

    /**
     * Private to hide implicit one.
     */
    private MessageHandlerRegistry()
    {
        /*
         * Intentionally left empty.
         */
    }

    /**
     * Method to wrap an incoming message.
     * Loops through all existing message wrappers.
     * @param message the incoming message.
     * @param ctx the message context.
     * @param server the receiving server.
     * @param sender the actual sender.
     */
    public static void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
    {
        for (final IMessageHandler handler : handlers)
        {
            if (handler.canHandle(message))
            {
                handler.wrap(message, ctx, server, sender);
                return;
            }
        }
    }

    /**
     * Method to handle an incoming message.
     * Loops through all existing message handlers.
     * @param message the incoming message.
     * @param server the handling server.
     */
    public static void handle(final IMessageWrapper message, final Server server)
    {
        for (final IMessageHandler handler : handlers)
        {
            if (handler.canHandle(message.getMessage()))
            {
                handler.handle(message, server);
                return;
            }
        }
    }

    /**
     * Method to get the inner message of an incoming message.
     * Loops through all existing message handlers.
     * @param message the incoming message.
     */
    public static byte[] getMsg(final MessageProto.Message message)
    {
        for (final IMessageHandler handler : handlers)
        {
            if (handler.canHandle(message))
            {
                return handler.getMessage(message).toByteArray();
            }
        }
        return new byte[0];
    }

    /**
     * Handler for text messages.
     */
    private static class TextMessageHandler implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            //Log.getLogger().warn("ServerReceiver: " + server.getServerData().getId() + " received Text: " + message.getTextMsg().getText());
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            /*
             * Intentionally left empty, no specific handling.
             */
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasTextMsg();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getTextMsg();
        }
    }

    /**
     * Handler for int messages.
     */
    private static class IntMessageHandler implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            Log.getLogger().warn("ServerReceiver: " + server.getServerData().getId() + " received Int: " + message.getIntMsg().getI());
            ctx.write(new IntMessageWrapper(sender, message));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            /*
             * Intentionally left empty, no specific handling.
             */
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasIntMsg();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getIntMsg();
        }
    }

    /**
     * Handler for join request messages.
     */
    private static class RegisterRequestMessage implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            Log.getLogger().warn("ServerReceiver received join request: " + server.getServerData().getId() + " ");
            server.addToInputQueue(new JoinRequestMessageWrapper(sender, message));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            if (server.view.getCoordinator() == server.getServerData().getId())
            {
                server.outputQueue.add(new BroadcastOperation(new RegisterMessageWrapper(server, (JoinRequestMessageWrapper) message)));
            }
            else
            {
                Log.getLogger().warn("Received join request at non-coordinator replica --- ignoring");
            }
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasReqRegMsg();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getReqRegMsg();
        }
    }

    /**
     * Handler for register messages.
     */
    private static class RegisterMessage implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            server.addToInputQueue(new RegisterMessageWrapper(sender, message));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            if (((RegisterMessageWrapper) message).sender == server.view.getCoordinator())
            {
                server.view.addServer(((RegisterMessageWrapper) message).getServerData());
                server.outputQueue.add(new ConnectOperation(((RegisterMessageWrapper) message).getServerData()));
            }
            else
            {
                Log.getLogger().warn("Non coordinator trying to register other replica!");
            }
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasRegMsg();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getRegMsg();
        }
    }

    /**
     * Handler for register messages.
     */
    private static class ClientMessage implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            try
            {
                if (!KeyUtilities.verifyKey(message.getClientMsg().toByteArray(), message.getSig().toByteArray(), new RSAPublicKeyImpl(message.getClientMsg().getPkey().toByteArray())))
                {
                    Log.getLogger().warn("Client sending message with invalid signature!");
                    return;
                }
            }
            catch (final InvalidKeyException e)
            {
                Log.getLogger().warn("Client sent message with invalid public key!", e);
                return;
            }

            server.clientInputQueue.add(new ClientMessageWrapper(sender, message));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            //Log.getLogger().warn("Received ClientMessage on: " + server.getServerData().getId());

            if (((AbstractMessageWrapper) message).sender == server.view.getCoordinator())
            {
                server.handleClientMessage(message.getMessage());
            }
            else
            {
                server.outputQueue.add(new UnicastOperation(message, server.view.getCoordinator()));
            }
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasClientMsg();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getClientMsg();
        }
    }

    /**
     * Handler for leave request messages.
     */
    private static class PersistClientMessage implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            Log.getLogger().warn("ServerReceiver received leave request: " + server.getServerData().getId() + " ");
            server.addToInputQueue(new PersistClientMessageWrapper(sender, message));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            final MessageProto.ClientMessage msg = message.getMessage().getPersClientMsg().getMsg();

            try
            {
                final PublicKey key = new RSAPublicKeyImpl(msg.getPkey().toByteArray());
                if (!KeyUtilities.verifyKey(msg.toByteArray(), message.getMessage().getPersClientMsg().getSig().toByteArray(),key ))
                {
                    Log.getLogger().warn("Invalid signature from client!");
                    return;
                }

                server.persist(msg);
            }
            catch (InvalidKeyException e)
            {
                Log.getLogger().warn("Invalid signature from client!");
                return;
            }
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasPersClientMsg();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getPersClientMsg();
        }
    }

    /**
     * Handler for leave request messages.
     */
    private static class UnregisterRequestMessage implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            Log.getLogger().warn("ServerReceiver received leave request: " + server.getServerData().getId() + " ");
            server.addToInputQueue(new UnregisterRequestMessageWrapper(sender, message));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            if (server.view.getCoordinator() == server.getServerData().getId())
            {
                server.outputQueue.add(new BroadcastOperation(new UnregisterMessageWrapper(server, message.getMessage())));
            }
            else
            {
                Log.getLogger().warn("Received unregister request at non-coordinator replica --- ignoring");
            }
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasReqUnregMsg();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getReqUnregMsg();
        }
    }

    /**
     * Handler for leave request messages.
     */
    private static class UnregisterMessage implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {
            Log.getLogger().warn("ServerReceiver received leave request: " + server.getServerData().getId() + " ");
            server.addToInputQueue(new UnregisterMessageWrapper(sender, message));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            if (((UnregisterMessageWrapper) message).sender == server.view.getCoordinator())
            {
                final ServerData data = ((UnregisterMessageWrapper) message).getServerData();
                final byte[] msg = MessageProto.RequestUnregisterMessage.newBuilder().setId(data.getId()).setIp(data.getIp()).setPort(data.getPort()).build().toByteArray();

                if (KeyUtilities.verifyKey(msg, ((UnregisterMessageWrapper) message).getSig(), server.view.getServer(data.getId()).getPublicKey()))
                {
                    server.unregister(data);
                }
                else
                {
                    Log.getLogger().warn("Invalid unregister request from coordinator, original replica did not sign request!!!");
                }
            }
            else
            {
                Log.getLogger().warn("Non coordinator trying to unregister other replica!");
            }
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasUnregMsg();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getUnregMsg();
        }
    }
}
