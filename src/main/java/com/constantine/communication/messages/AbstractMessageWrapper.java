package com.constantine.communication.messages;

import com.constantine.communication.nettyhandlers.SizedMessage;
import com.constantine.proto.MessageProto;
import com.constantine.server.IServer;

/**
 * Abstract implementation of the message wrapper.
 */
public abstract class AbstractMessageWrapper implements IMessageWrapper
{
    /**
     * The proto message builder to avoid creating a new builder for each message.
     */
    static final MessageProto.Message.Builder builder = MessageProto.Message.newBuilder();

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
        return new SizedMessage(buildMessage(serverSender), sender);
    }

    @Override
    public MessageProto.Message getMessage()
    {
        return message;
    }
}
