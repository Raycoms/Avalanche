package com.ray.mcu.communication.wrappers;

import com.google.protobuf.GeneratedMessageV3;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.google.protobuf.ByteString;

/**
 * Example register Message.
 */
public class PersistClientMessageWrapper extends AbstractMessageWrapper
{
    /**
     * Create an instance of the register message wrapper.
     * @param sender the sender.
     * @param message the join request resulting in the register.
     */
    public PersistClientMessageWrapper(final IServer sender, final MessageProto.PersistClientMessage message)
    {
        super(sender.getServerData().getId(), MessageProto.Message.newBuilder().setPersClientMsg(message));
    }

    /**
     * Create an instance of the register message wrapper.
     * @param sender the sender.
     * @param message the join request resulting in the register.
     */
    public PersistClientMessageWrapper(final IServer sender, final MessageProto.ClientMessage message, final ByteString sig)
    {
        this(sender, MessageProto.PersistClientMessage.newBuilder().setMsg(message).setSig(sig).build());
        this.alreadySigned = true;
    }

    /**
     * Create an instance of the register message wrapper.
     * @param sender the sender.
     * @param message the join request resulting in the register.
     */
    public PersistClientMessageWrapper(final int sender, final MessageProto.Message.Builder message)
    {
        super(sender, message);
    }

    @Override
    public GeneratedMessageV3 getPackagedMessage()
    {
        return message.getPersClientMsg();
    }
}
