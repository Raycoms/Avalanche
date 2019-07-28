package com.constantine.communication;

import com.constantine.proto.MessageProto;
import com.constantine.communication.message.SizedMessage;
import com.constantine.server.Server;
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
     * message context.
     */
    private ChannelHandlerContext ctx;

    /**
     * The ServerReceiver this client handler belongs to.
     */
    private Server server;

    /**
     * The ServerReceiver this client handler connects to.
     */
    private final ServerData serverData;


    /**
     * Start the NettySenderHandler with a server instance.
     * @param server the server instance to use.
     */
    public NettySenderHandler(final Server server, final ServerData serverData)
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
            final MessageProto.Message message = MessageProto.Message.parseFrom(msg.buffer);
            final MessageProto.Message.Builder builder = MessageProto.Message.newBuilder();

            if (message.hasTextMsg())
            {
                Log.getLogger().warn("ServerSender: " + server.getServerData().getId() + " received Text: " + message.getTextMsg().getText());
                final MessageProto.TextMessage.Builder textBuilder = MessageProto.TextMessage.newBuilder();
                builder.setTextMsg(textBuilder.setText(message.getTextMsg().getText() + " return!").build());

            }
            else if (message.hasIntMsg())
            {
                Log.getLogger().warn("ServerSender: " + server.getServerData().getId() + " received Int: " + message.getIntMsg().getI());
                final MessageProto.IntMessage.Builder intBuilder = MessageProto.IntMessage.newBuilder();
                builder.setIntMsg(intBuilder.setI(message.getIntMsg().getI() + 1).build());
            }

            //Create message
            final SizedMessage sizedMessage = new SizedMessage(builder.build().toByteArray());
            ctx.write(sizedMessage);
        }
        catch (InvalidProtocolBufferException e)
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
     * Write a string message to the channel.
     * @param string the string to write.
     */
    public void write(final String string)
    {
        final MessageProto.Message.Builder builder = MessageProto.Message.newBuilder();
        final MessageProto.IntMessage.Builder intBuilder = MessageProto.IntMessage.newBuilder();
        builder.setIntMsg(intBuilder.setI(0).build());
        final SizedMessage sizedMessage = new SizedMessage(builder.build().toByteArray());

        this.ctx.writeAndFlush(sizedMessage);
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
}