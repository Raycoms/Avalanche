package com.constantine.communication;

import com.constantine.proto.MessageProto;
import com.constantine.communication.message.SizedMessage;
import com.constantine.server.Server;
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
            final MessageProto.Message.Builder builder = MessageProto.Message.newBuilder();

            if (message.hasTextMsg())
            {
                Log.getLogger().warn("ServerReceiver: " + server.getServerData().getId() + " received Text: " + message.getTextMsg().getText());
                final MessageProto.TextMessage.Builder textBuilder = MessageProto.TextMessage.newBuilder();
                builder.setTextMsg(textBuilder.setText(message.getTextMsg().getText() + " return!").build());

            }
            else if (message.hasIntMsg())
            {
                Log.getLogger().warn("ServerReceiver: " + server.getServerData().getId() + " received Int: " + message.getIntMsg().getI());
                final MessageProto.IntMessage.Builder intBuilder = MessageProto.IntMessage.newBuilder();
                builder.setIntMsg(intBuilder.setI(message.getIntMsg().getI() + 1).build());
            }
            else if (message.hasRegMsg())
            {

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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        cause.printStackTrace();
        ctx.close();
    }
}
