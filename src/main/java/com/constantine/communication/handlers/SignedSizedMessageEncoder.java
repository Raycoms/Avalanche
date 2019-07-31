package com.constantine.communication.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Message encoder for all netty messages for the direct access client/server communication.
 */
public class SignedSizedMessageEncoder extends MessageToByteEncoder<SignedSizedMessage>
{
    @Override
    protected void encode(final ChannelHandlerContext ctx, final SignedSizedMessage msg, final ByteBuf out)
    {
        out.writeInt(msg.id);
        out.writeInt(msg.buffer.length);
        out.writeBytes(msg.buffer);

        out.writeInt(msg.sig.length);
        out.writeBytes(msg.sig);
    }
}
