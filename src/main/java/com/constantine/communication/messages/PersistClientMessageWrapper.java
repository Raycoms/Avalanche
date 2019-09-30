package com.constantine.communication.messages;

import com.constantine.proto.MessageProto;
import com.constantine.server.IServer;
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
        super(sender.getServerData().getId(), builder.setPersClientMsg(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
    }

    /**
     * Create an instance of the register message wrapper.
     * @param sender the sender.
     * @param message the join request resulting in the register.
     */
    public PersistClientMessageWrapper(final IServer sender, final MessageProto.ClientMessage message, final ByteString sig)
    {
        this(sender, MessageProto.PersistClientMessage.newBuilder().setMsg(message).setSig(sig).build());
    }

    @Override
    public byte[] buildMessage()
    {
        return this.message.toByteArray();
    }
}
