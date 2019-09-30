package com.constantine.communication;

import com.constantine.communication.messages.IMessageWrapper;
import com.constantine.proto.MessageProto;
import com.constantine.server.Server;
import io.netty.channel.ChannelHandlerContext;

/**
 * Specific message handler interface to handle incoming messages.
 */
public interface IMessageHandler
{
    /**
     * Specific message wrapper.
     * @param message the message to wrap.
     * @param ctx the incoming message context.
     * @param server the receiving server.
     */
    public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server);

    /**
     * Specific message handler.
     * @param message the message to handle.
     * @param server the handling server.
     */
    public void handle(final IMessageWrapper message, final Server server);

    /**
     * Check if this message handler can handle this specific message.
     * @param message the message to check.
     * @return true if so.
     */
    public boolean canHandle(final MessageProto.Message message);
}
