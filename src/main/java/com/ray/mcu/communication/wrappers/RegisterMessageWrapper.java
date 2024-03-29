package com.ray.mcu.communication.wrappers;

import com.google.protobuf.GeneratedMessageV3;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.ray.mcu.server.ServerData;
import com.google.protobuf.ByteString;

/**
 * Example register Message.
 */
public class RegisterMessageWrapper extends AbstractMessageWrapper
{
    /**
     * Create an instance of the register message wrapper.
     * @param message the message to extract it from.
     * @param sender the sender.
     */
    public RegisterMessageWrapper(final IServer sender, final MessageProto.RegisterMessage message)
    {
        super(sender.getServerData().getId(), MessageProto.Message.newBuilder().setRegMsg(message));
    }

    /**
     * Create an instance of the register message wrapper.
     * @param message the join request resulting in the register.
     */
    public RegisterMessageWrapper(final IServer sender, final JoinRequestMessageWrapper message)
    {
        this(sender, MessageProto.RegisterMessage.newBuilder().setId(message.getServerData().getId()).setIp(message.getServerData().getIp()).setPort(message.getServerData().getPort()).build());
    }

    /**
     * Create an instance of the register message wrapper.
     * @param message the message to extract it from.
     * @param sender the sender.
     */
    public RegisterMessageWrapper(final int sender, final MessageProto.Message.Builder message)
    {
        super(sender, message);
    }

    /**
     * Get the associated server data.
     * @return the server data.
     */
    public ServerData getServerData()
    {
        final MessageProto.RegisterMessage msg = this.message.getRegMsg();
        return new ServerData(msg.getId(), msg.getIp(), msg.getPort());
    }

    @Override
    public GeneratedMessageV3 getPackagedMessage()
    {
        return message.getRegMsg();
    }
}
