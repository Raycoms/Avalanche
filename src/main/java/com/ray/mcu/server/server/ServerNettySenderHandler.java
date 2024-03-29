package com.ray.mcu.server.server;

import com.ray.mcu.communication.wrappers.IMessageWrapper;
import com.ray.mcu.nettyhandlers.SizedMessage;
import com.ray.mcu.server.IServer;
import com.ray.mcu.server.ServerData;
import com.ray.mcu.utils.Log;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles a server side sending channel.
 */
@ChannelHandler.Sharable
public class ServerNettySenderHandler extends SimpleChannelInboundHandler<SizedMessage>
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
     * The server data we are sending the data to.
     */
    private final ServerData serverData;

    /**
     * If the client is currently trying to reconnect.
     */
    private boolean isReconnecting = false;

    /**
     * Cache which holds the messages to send in the future (due to downtime of connection)
     */
    public final ConcurrentLinkedQueue<IMessageWrapper> outputQueue = new ConcurrentLinkedQueue<>();

    /**
     * Start the NettySenderHandler with a server instance.
     * @param server the server instance to use.
     */
    public ServerNettySenderHandler(final IServer server, final ServerData serverData)
    {
        this.server = server;
        this.serverData = serverData;
    }

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext)
    {
        ctx = channelHandlerContext;
        isReconnecting = false;
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception
    {
        super.channelInactive(ctx);
        this.ctx = null;
        isReconnecting = false;
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext channelHandlerContext, final Throwable cause)
    {
        cause.printStackTrace();
        channelHandlerContext.close();
        isReconnecting = false;
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
            // If there are still pending messages.
            if (!outputQueue.isEmpty())
            {
                // Poll messages from buffer and send them.
                while (outputQueue.peek() != null)
                {
                    Log.getLogger().warn("Sending out queued object!!!!");
                    this.ctx.writeAndFlush(outputQueue.poll());
                }
            }

            this.ctx.writeAndFlush(msg.writeToSizedMessage(server));
            return true;
        }
        else
        {
            Log.getLogger().warn("Queue add");
            outputQueue.add(msg);
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
     * Get the id of the server this belongs to.
     * @return the server id.
     */
    public int getId()
    {
        return server.getServerData().getId();
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

    /**
     * Check if the server is currently reconnecting.
     * @return true if so.
     */
    public boolean isReconnecting()
    {
        return isReconnecting;
    }

    /**
     * Set that this is currently reconnecting.
     * @param reconnecting true if so.
     */
    public void setReconnecting(final boolean reconnecting)
    {
        isReconnecting = reconnecting;
    }
}