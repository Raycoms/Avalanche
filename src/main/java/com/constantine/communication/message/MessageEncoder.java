package com.constantine.communication.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Message encoder for all netty messages for the direct access client/server communication.
 */
public class MessageEncoder extends MessageToByteEncoder<SizedMessage>
{
    @Override
    protected void encode(final ChannelHandlerContext ctx, final SizedMessage msg, final ByteBuf out)
    {
        out.writeInt(msg.size);
        out.writeBytes(msg.buffer);
    }
}
