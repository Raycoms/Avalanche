package com.ray.mcu.server.client;

import com.ray.mcu.communication.wrappers.IMessageWrapper;
import com.ray.mcu.nettyhandlers.SizedMessage;
import com.ray.mcu.server.IServer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles a server side sending channel.
 */
@ChannelHandler.Sharable
public class ServerNettyClientSenderHandler extends SimpleChannelInboundHandler<SizedMessage>
{
    /**
     * handlers context.
     */
    private ChannelHandlerContext ctx;

    /**
     * The ServerReceiver this client handler belongs to.
     */
    private IServer server;

    /**
     * Start the NettySenderHandler with a server instance.
     * @param server the server instance to use.
     */
    public ServerNettyClientSenderHandler(final IServer server)
    {
        this.server = server;
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
     * Write a a IMessageWrapper and send it.
     * @param msg the msg to send.
     * @return true if successful.
     */
    public boolean write(final IMessageWrapper msg)
    {
        if (isActive())
        {
            this.ctx.writeAndFlush(msg.writeToSizedMessage(server));
            return true;
        }
        else
        {
            return false;
        }
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
}