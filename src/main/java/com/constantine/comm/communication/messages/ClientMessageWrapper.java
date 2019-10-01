package com.constantine.comm.communication.messages;

import com.constantine.comm.proto.MessageProto;
import com.constantine.comm.server.IServer;

/**
 * Example register Message.
 */
public class ClientMessageWrapper extends AbstractMessageWrapper
{
    /**
     * Create an instance of the register message wrapper.
     * @param sender the sender.
     * @param message the join request resulting in the register.
     */
    public ClientMessageWrapper(final IServer sender, final MessageProto.Message message)
    {
        super(sender.getServerData().getId(), message);
    }

    /**
     * Create an instance of the register message wrapper.
     * @param sender the sender.
     * @param message the join request resulting in the register.
     */
    public ClientMessageWrapper(final int sender, final MessageProto.Message message)
    {
        super(sender, message);
    }

    @Override
    public byte[] buildMessage()
    {
        return this.message.toByteArray();
    }
}
