package com.constantine.communication.messages;

import com.constantine.proto.MessageProto;
import com.constantine.server.IServer;
import com.google.protobuf.ByteString;

/**
 * Example int Message.
 */
public class IntMessageWrapper extends AbstractMessageWrapper
{
    /**
     * Create an instance of the int message wrapper.
     * @param message the message to send.
     * @param sender the sender.
     */
    public IntMessageWrapper(final IServer sender, final MessageProto.IntMessage message)
    {
        super(sender.getServerData().getId(), builder.setIntMsg(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
    }

    /**
     * Create an instance of the int message wrapper.
     * @param message the int to send.
     * @param sender the sender.
     */
    public IntMessageWrapper(final IServer sender, final int message)
    {
        this(sender, MessageProto.IntMessage.newBuilder().setI(message).build());
    }

    @Override
    public byte[] buildMessage()
    {
        return message.toByteArray();
    }

    /**
     * Get the int message from the wrapper.
     * @return the int.
     */
    public int getInt()
    {
        return this.message.getIntMsg().getI();
    }
}
