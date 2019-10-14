package com.ray.mcu.server.server;

import com.ray.mcu.communication.MessageHandlerRegistry;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.nettyhandlers.SizedMessage;
import com.ray.mcu.server.Server;
import com.ray.mcu.utils.Log;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Arrays;

/**
 * Handles a server-side channel.
 */
public class ServerNettyReceiverHandler extends SimpleChannelInboundHandler<SizedMessage>
{
    /**
     * Current server - server latency on reception.
     * todo: make it configurable
     */
    private static final long LATENCY = 20;

    /**
     * The connected server instance.
     */
    private final Server server;

    /**
     * Constructor to create a new NettyReceiverHandler.
     * @param server the server it belongs to.
     */
    public ServerNettyReceiverHandler(final Server server)
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
            if (message.hasSig() && server.view.getServer(msg.id) != null)
            {
                final byte[] m = MessageHandlerRegistry.getMsg(message);
                final byte[] s = message.getSig().toByteArray();
                if (!server.view.getServer(msg.id).verifyKey(m, s))
                {
                    Log.getLogger().error("----------------------------------------------------------");
                    Log.getLogger().error(server.getServerData().getId() + " Received invalid signature supposedly from replica: " + msg.id);
                    Log.getLogger().warn(Arrays.toString(m));
                    Log.getLogger().error("Discarding Message");
                    Log.getLogger().error("----------------------------------------------------------");
                    return;
                }
            }

            try
            {
                Thread.sleep(LATENCY);
            }
            catch (final InterruptedException e)
            {
                e.printStackTrace();
            }

            MessageHandlerRegistry.wrap(message, ctx, server, msg.id);
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
