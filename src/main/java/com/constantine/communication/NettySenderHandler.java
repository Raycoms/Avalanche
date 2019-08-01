package com.constantine.communication;

import com.constantine.communication.messages.IMessageWrapper;
import com.constantine.communication.messages.IntMessageWrapper;
import com.constantine.communication.messages.TextMessageWrapper;
import com.constantine.proto.MessageProto;
import com.constantine.communication.handlers.SizedMessage;
import com.constantine.server.IServer;
import com.constantine.server.ServerData;
import com.constantine.utils.Log;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles a server side sending channel.
 */
public class NettySenderHandler extends SimpleChannelInboundHandler<SizedMessage>
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
     * The server data of this client.
     */
    private final ServerData serverData;

    /**
     * Start the NettySenderHandler with a server instance.
     * @param server the server instance to use.
     */
    public NettySenderHandler(final IServer server, final ServerData serverData)
    {
        this.server = server;
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
        //todo attempt reconnect
        //todo start moving messages into buffer
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
        try
        {
            //Read input
            //todo this is only for testing!
            final MessageProto.Message message = MessageProto.Message.parseFrom(msg.buffer);
            if (message.hasTextMsg())
            {
                Log.getLogger().warn("ServerSender: " + server.getServerData().getId() + " received Text: " + message.getTextMsg().getText());
                ctx.write(new TextMessageWrapper(message.getTextMsg().getText() + " return!", this.getId()));
            }
            else if (message.hasIntMsg())
            {
                Log.getLogger().warn("ServerSender: " + server.getServerData().getId() + " received Int: " + message.getIntMsg().getI());
                ctx.write(new IntMessageWrapper(message.getIntMsg().getI() + 1, this.getId()));
            }
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

    /**
     * Write a a IMessageWrapper and send it.
     * @param msg the msg to send.
     */
    public void write(final IMessageWrapper msg)
    {
        this.ctx.writeAndFlush(msg.writeToSizedMessage());
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
}