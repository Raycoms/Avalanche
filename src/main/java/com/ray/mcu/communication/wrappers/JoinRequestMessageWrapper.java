package com.ray.mcu.communication.wrappers;

import com.google.protobuf.GeneratedMessageV3;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.ray.mcu.server.ServerData;
import com.google.protobuf.ByteString;

/**
 * Example join request message wrapper.
 */
public class JoinRequestMessageWrapper extends AbstractMessageWrapper
{
    /**
     * Create an instance of the join request message wrapper.
     * @param message the message to send.
     * @param sender the sender.
     */
    public JoinRequestMessageWrapper(final IServer sender, final MessageProto.RequestRegisterMessage message)
    {
        super(sender.getServerData().getId(), MessageProto.Message.newBuilder().setReqRegMsg(message));
    }

    /**
     * Create an instance of the join request message wrapper.
     * @param message the int to send.
     * @param sender the sender.
     */
    public JoinRequestMessageWrapper(final IServer sender, final ServerData message)
    {
        this(sender, MessageProto.RequestRegisterMessage.newBuilder().setId(message.getId()).setIp(message.getIp()).setPort(message.getPort()).build());
    }

    /**
     * Create an instance of the join request message wrapper.
     * @param message the int to send.
     * @param sender the sender.
     */
    public JoinRequestMessageWrapper(final int sender, final MessageProto.Message.Builder message)
    {
        super(sender, message);
    }

    /**
     * Get the associated server data.
     * @return the server data.
     */
    public ServerData getServerData()
    {
        final MessageProto.RequestRegisterMessage msg = this.message.getReqRegMsg();
        return new ServerData(msg.getId(), msg.getIp(), msg.getPort());
    }

    @Override
    public GeneratedMessageV3 getPackagedMessage()
    {
        return message.getReqRegMsg();
    }
}
