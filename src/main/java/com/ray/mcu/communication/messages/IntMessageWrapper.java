package com.ray.mcu.communication.messages;

import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
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
        super(sender.getServerData().getId(), MessageProto.Message.newBuilder().setIntMsg(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
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

    /**
     * Copy an int message into the wrapper.
     * @param sender the sender.
     * @param message the message.
     */
    public IntMessageWrapper(final int sender, final MessageProto.Message message)
    {
        super(sender, message);
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
