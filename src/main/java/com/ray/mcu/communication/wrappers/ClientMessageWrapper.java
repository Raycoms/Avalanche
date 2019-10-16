package com.ray.mcu.communication.wrappers;

import com.google.protobuf.GeneratedMessageV3;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;

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
    public ClientMessageWrapper(final IServer sender, final MessageProto.Message.Builder message)
    {
        super(sender.getServerData().getId(), message);
        this.alreadySigned = true;
    }

    /**
     * Create an instance of the register message wrapper.
     * @param sender the sender.
     * @param message the join request resulting in the register.
     */
    public ClientMessageWrapper(final int sender, final MessageProto.Message.Builder message)
    {
        super(sender, message);
        this.alreadySigned = true;
    }

    @Override
    public byte[] buildMessage(final IServer serverSender)
    {
        return this.message.build().toByteArray();
    }

    @Override
    public GeneratedMessageV3 getPackagedMessage()
    {
        return this.message.getClientMsg();
    }
}
