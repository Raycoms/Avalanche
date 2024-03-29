package com.ray.mcu.communication;

import com.ray.mcu.communication.wrappers.IMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.Server;
import com.google.protobuf.GeneratedMessageV3;
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
     * @param sender the sender id.
     */
    public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender);

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

    /**
     * Get the sent message of a proto message.
     * @param message the proto message.
     * @return the inner generated message.
     */
    public GeneratedMessageV3 getMessage(final MessageProto.Message message);
}
