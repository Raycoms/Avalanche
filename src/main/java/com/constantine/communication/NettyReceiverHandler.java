package com.constantine.communication;

import com.constantine.proto.MessageProto;
import com.constantine.communication.nettyhandlers.SizedMessage;
import com.constantine.server.Server;
import com.constantine.utils.KeyUtilities;
import com.constantine.utils.Log;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles a server-side channel.
 */
public class NettyReceiverHandler extends SimpleChannelInboundHandler<SizedMessage>
{
    /**
     * The connected server instance.
     */
    private final Server server;

    /**
     * Constructor to create a new NettyReceiverHandler.
     * @param server the server it belongs to.
     */
    public NettyReceiverHandler(final Server server)
    {
        this.server = server;
    }

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final SizedMessage msg)
    {
        try
        {
            //Read input
            final MessageProto.Message message = MessageProto.Message.parseFrom(msg.buffer);
            if (message.hasSig())
            {
                if (!KeyUtilities.verifyKey(msg.buffer, message.getSig().toByteArray(), server.view.getServer(msg.id).getPublicKey()))
                {
                    Log.getLogger().error("----------------------------------------------------------");
                    Log.getLogger().error("Received invalid signature supposedly from replica: " + msg.id);
                    Log.getLogger().error("Discarding Message");
                    Log.getLogger().error("----------------------------------------------------------");
                    return;
                }
            }

            MessageHandlerRegistry.wrap(message, ctx, server);
        }
        catch (final InvalidProtocolBufferException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx)
    {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        cause.printStackTrace();
        ctx.close();
    }
}
