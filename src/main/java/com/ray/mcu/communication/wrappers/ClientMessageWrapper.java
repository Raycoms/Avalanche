package com.ray.mcu.communication.wrappers;

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
}
