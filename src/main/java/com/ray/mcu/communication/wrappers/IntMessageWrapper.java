package com.ray.mcu.communication.wrappers;

import com.google.protobuf.GeneratedMessageV3;
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
        super(sender.getServerData().getId(), MessageProto.Message.newBuilder().setIntMsg(message));
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
    public IntMessageWrapper(final int sender, final MessageProto.Message.Builder message)
    {
        super(sender, message);
    }

    /**
     * Get the int message from the wrapper.
     * @return the int.
     */
    public int getInt()
    {
        return this.message.getIntMsg().getI();
    }

    @Override
    public GeneratedMessageV3 getPackagedMessage()
    {
        return message.getIntMsg();
    }
}
