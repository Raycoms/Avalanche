package com.constantine.communication;

import com.constantine.communication.messages.*;
import com.constantine.communication.operations.BroadcastOperation;
import com.constantine.communication.operations.ConnectOperation;
import com.constantine.communication.operations.DisconnectOperation;
import com.constantine.communication.operations.UnicastOperation;
import com.constantine.proto.MessageProto;
import com.constantine.server.Server;
import com.constantine.server.ServerData;
import com.constantine.utils.KeyUtilities;
import com.constantine.utils.Log;
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
     */
    public static void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server)
    {
        for (final IMessageHandler handler : handlers)
        {
            if (handler.canHandle(message))
            {
                handler.wrap(message, ctx, server);
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
     * Handler for text messages.
     */
    private static class TextMessageHandler implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server)
        {
            Log.getLogger().warn("ServerReceiver: " + server.getServerData().getId() + " received Text: " + message.getTextMsg().getText());
            ctx.write(new TextMessageWrapper(server, message.getTextMsg().getText() + " return!"));
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
    }

    /**
     * Handler for int messages.
     */
    private static class IntMessageHandler implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server)
        {
            Log.getLogger().warn("ServerReceiver: " + server.getServerData().getId() + " received Int: " + message.getIntMsg().getI());
            ctx.write(new IntMessageWrapper(server, message.getIntMsg().getI() + 1));
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
    }

    /**
     * Handler for join request messages.
     */
    private static class RegisterRequestMessage implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server)
        {
            Log.getLogger().warn("ServerReceiver received join request: " + server.getServerData().getId() + " ");
            server.inputQueue.add(new JoinRequestMessageWrapper(server, message.getReqRegMsg()));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            if (server.view.getCoordinator() == server.getId())
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
    }

    /**
     * Handler for register messages.
     */
    private static class RegisterMessage implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server)
        {
            server.inputQueue.add(new RegisterMessageWrapper(server, message.getRegMsg()));
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
    }

    /**
     * Handler for register messages.
     */
    private static class ClientMessage implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server)
        {
            server.clientInputQueue.add(new ClientMessageWrapper(server, message));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            if (((RegisterMessageWrapper) message).sender == server.view.getCoordinator())
            {
                server.outputQueue.add(new BroadcastOperation(new PersistClientMessageWrapper(server, message.getMessage().getClientMsg(), message.getMessage().getSig())));
            }
            else
            {
                server.outputQueue.add(new UnicastOperation(message, server.view.getCoordinator()));
                Log.getLogger().warn("Non coordinator trying to register other replica!");
            }
        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasClientMsg();
        }
    }

    /**
     * Handler for leave request messages.
     */
    private static class PersistClientMessage implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server)
        {
            Log.getLogger().warn("ServerReceiver received leave request: " + server.getServerData().getId() + " ");
            server.inputQueue.add(new PersistClientMessageWrapper(server, message.getPersClientMsg()));
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

                int tempState = server.state.get(key);
                tempState += msg.getDif();
                server.state.put(key, tempState);
            }
            catch (InvalidKeyException e)
            {
                Log.getLogger().warn("Invalid signature from client!");
                return;
            }

            if (server.view.getCoordinator() == server.getId())
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
    }

    /**
     * Handler for leave request messages.
     */
    private static class UnregisterRequestMessage implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server)
        {
            Log.getLogger().warn("ServerReceiver received leave request: " + server.getServerData().getId() + " ");
            server.inputQueue.add(new UnregisterRequestMessageWrapper(server, message.getReqUnregMsg()));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            if (server.view.getCoordinator() == server.getId())
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
    }

    /**
     * Handler for leave request messages.
     */
    private static class UnregisterMessage implements IMessageHandler
    {
        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server)
        {
            Log.getLogger().warn("ServerReceiver received leave request: " + server.getServerData().getId() + " ");
            server.inputQueue.add(new UnregisterMessageWrapper(server, message));
        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {
            if (((UnregisterMessageWrapper) message).sender == server.view.getCoordinator())
            {
                final ServerData data = ((UnregisterMessageWrapper) message).getServerData();
                final byte[] msg = MessageProto.UnregisterMessage.newBuilder().setId(data.getId()).setIp(data.getIp()).setPort(data.getPort()).build().toByteArray();

                if (KeyUtilities.verifyKey(msg, ((UnregisterMessageWrapper) message).getSig(), server.view.getServer(data.getId()).getPublicKey()))
                {
                    server.view.removeServer(data.getId());
                    server.outputQueue.add(new DisconnectOperation(data));
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
    }
}
