package com.ray.mcu.communication.wrappers;

import com.ray.mcu.nettyhandlers.SizedMessage;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;

/**
 * Abstract implementation of the message wrapper.
 */
public abstract class AbstractMessageWrapper implements IMessageWrapper
{
    /**
     * Id of the sender.
     */
    public final int sender;

    /**
     * The incoming message.
     */
    public final MessageProto.Message message;

    /**
     * Create an instance of abstract wrapper.
     * @param sender the sender.
     * @param message the message.
     */
    public AbstractMessageWrapper(final int sender, final MessageProto.Message message)
    {
        this.sender = sender;
        this.message = message;
    }

    @Override
    public SizedMessage writeToSizedMessage(final IServer serverSender)
    {
        return new SizedMessage(buildMessage(), this.sender);
    }

    @Override
    public MessageProto.Message getMessage()
    {
        return message;
    }

    /**
     * Get the supposed sender from the message.
     * @return the sender id.
     */
    public int getSender()
    {
        return sender;
    }
}
