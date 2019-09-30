package com.constantine.client;

import com.constantine.communication.messages.IMessageWrapper;
import com.constantine.communication.messages.IntMessageWrapper;
import com.constantine.communication.messages.TextMessageWrapper;
import com.constantine.nettyhandlers.SizedMessage;
import com.constantine.proto.MessageProto;
import com.constantine.server.IServer;
import com.constantine.server.ServerData;
import com.constantine.utils.Log;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles a server side sending channel.
 */
@ChannelHandler.Sharable
public class ClientNettySenderHandler extends SimpleChannelInboundHandler<SizedMessage>
{
    /**
     * handlers context.
     */
    private ChannelHandlerContext ctx;

    /**
     * The server data we are sending the data to.
     */
    private final ServerData serverData;

    /**
     * Start the NettySenderHandler with a server instance.
     * @param serverData the server data to conntext to.
     */
    public ClientNettySenderHandler(final ServerData serverData)
    {
        this.serverData = serverData;
    }

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext)
    {
        ctx = channelHandlerContext;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception
    {
        super.channelInactive(ctx);
        this.ctx = null;
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext channelHandlerContext, final Throwable cause)
    {
        cause.printStackTrace();
        channelHandlerContext.close();
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final SizedMessage msg) throws Exception
    {
        /*
         * Intentionally left empty.
         */
    }

    @Override
    public void channelReadComplete(final ChannelHandlerContext ctx)
    {
        ctx.flush();
    }

    /**
     * Write a proto message and send it.
     * @param msg the msg to send.
     */
    public void write(final MessageProto.Message msg)
    {
        this.ctx.writeAndFlush(msg.toByteArray());
    }

    /**
     * Check if the connection is active.
     * @return true if so.
     */
    public boolean isActive()
    {
        return ctx != null;
    }

    /**
     * Disconnect the existing context.
     */
    public void disconnect()
    {
        if (ctx != null)
        {
            ctx.disconnect();
            ctx = null;
        }
    }

    /**
     * Get the server associated to this connection.
     * @return the data.
     */
    public ServerData getServerData()
    {
        return serverData;
    }
}