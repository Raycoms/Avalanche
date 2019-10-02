package com.ray.mcu.client;

import com.ray.mcu.nettyhandlers.SizedMessage;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.utils.KeyUtilities;
import com.ray.mcu.utils.Log;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles a client-side channel.
 */
public class ClientNettyReceiverHandler extends SimpleChannelInboundHandler<SizedMessage>
{
    /**
     * The connected client instance.
     */
    private final Client client;

    /**
     * Constructor to create a new NettyReceiverHandler.
     * @param client the client it belongs to.
     */
    public ClientNettyReceiverHandler(final Client client)
    {
        this.client = client;
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
                if (!KeyUtilities.verifyKey(msg.buffer, message.getSig().toByteArray(), client.view.getServer(msg.id).getPublicKey()))
                {
                    Log.getLogger().error("----------------------------------------------------------");
                    Log.getLogger().error("Received invalid signature supposedly from client: ");
                    Log.getLogger().error("Discarding Message");
                    Log.getLogger().error("----------------------------------------------------------");
                    return;
                }
            }

            Log.getLogger().warn("Received a message on client with result: " + (message.getResponse().getResponse() ? "Success" : "Failure"));
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
