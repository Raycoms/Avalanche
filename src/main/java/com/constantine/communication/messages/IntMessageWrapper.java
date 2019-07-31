package com.constantine.communication.messages;

import com.constantine.communication.handlers.SizedMessage;
import com.constantine.proto.MessageProto;

/**
 * Example int Message.
 */
public class IntMessageWrapper implements IMessageWrapper
{
    /**
     * The String of this message.
     */
    private final int message;

    /**
     * Id of the sender.
     */
    public final int sender;

    /**
     * Create an instance of the int message wrapper.
     * @param message the int to send.
     * @param sender the sender.
     */
    public IntMessageWrapper(final int message, final int sender)
    {
        this.message = message;
        this.sender = sender;
    }

    /**
     * Create an instance of the int message wrapper.
     * @param message the message to extract it from.
     * @param sender the sender.
     */
    public IntMessageWrapper(final MessageProto.IntMessage message, final int sender)
    {
        this.message = message.getI();
        this.sender = sender;
    }

    @Override
    public SizedMessage writeToSizedMessage()
    {
        final MessageProto.IntMessage.Builder intBuilder = MessageProto.IntMessage.newBuilder();
        builder.setIntMsg(intBuilder.setI(this.message).build());

        return new SizedMessage(builder.build().toByteArray(), sender);
    }
}
