package com.constantine.communication;

import com.constantine.communication.messages.IntMessageWrapper;
import com.constantine.communication.messages.JoinRequestMessageWrapper;
import com.constantine.communication.messages.RegisterMessageWrapper;
import com.constantine.communication.messages.TextMessageWrapper;
import com.constantine.proto.MessageProto;
import com.constantine.communication.handlers.SizedMessage;
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
            Log.getLogger().warn("msg!");
            //Read input
            final MessageProto.Message message = MessageProto.Message.parseFrom(msg.buffer);
            if (message.hasTextMsg())
            {
                Log.getLogger().warn("ServerReceiver: " + server.getServerData().getId() + " received Text: " + message.getTextMsg().getText());
                ctx.write(new TextMessageWrapper(message.getTextMsg().getText() + " return!", server.getServerData().getId()));
            }
            else if (message.hasIntMsg())
            {
                Log.getLogger().warn("ServerReceiver: " + server.getServerData().getId() + " received Int: " + message.getIntMsg().getI());
                ctx.write(new IntMessageWrapper(message.getIntMsg().getI() + 1, server.getServerData().getId()));
            }
            else if (message.hasReqRegMsg())
            {
                Log.getLogger().warn("ServerReceiver received join request: " + server.getServerData().getId() + " ");
                server.inputQueue.add(new JoinRequestMessageWrapper(message.getReqRegMsg(), server.getServerData().getId()));
            }
            else if (message.hasRegMsg())
            {
                server.inputQueue.add(new RegisterMessageWrapper(message.getRegMsg(), server.getServerData().getId()));
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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
    {
        cause.printStackTrace();
        ctx.close();
    }
}
