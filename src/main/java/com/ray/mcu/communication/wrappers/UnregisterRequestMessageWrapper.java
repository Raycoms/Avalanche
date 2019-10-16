package com.ray.mcu.communication.wrappers;

import com.google.protobuf.GeneratedMessageV3;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.ray.mcu.server.ServerData;
import com.google.protobuf.ByteString;

/**
 * Example unregister request message wrapper.
 */
public class UnregisterRequestMessageWrapper extends AbstractMessageWrapper
{
    /**
     * Create an instance of the unregister message wrapper.
     * @param message the message to extract it from.
     * @param sender the sender.
     */
    public UnregisterRequestMessageWrapper(final IServer sender, final MessageProto.RequestUnregisterMessage message)
    {
        super(sender.getServerData().getId(), MessageProto.Message.newBuilder().setReqUnregMsg(message));
    }

    /**
     * Create an instance of the unregister request message wrapper.
     * @param message the int to send.
     */
    public UnregisterRequestMessageWrapper(final IServer sender, final ServerData message)
    {
        this(sender, MessageProto.RequestUnregisterMessage.newBuilder().setId(message.getId()).setIp(message.getIp()).setPort(message.getPort()).build());
    }

    /**
     * Create an instance of the unregister message wrapper.
     * @param message the message to extract it from.
     * @param sender the sender.
     */
    public UnregisterRequestMessageWrapper(final int sender, final MessageProto.Message.Builder message)
    {
        super(sender, message);
    }

    /**
     * Get the associated server data.
     * @return the server data.
     */
    public ServerData getServerData()
    {
        final MessageProto.RequestUnregisterMessage msg = this.message.getReqUnregMsg();
        return new ServerData(msg.getId(), msg.getIp(), msg.getPort());
    }

    @Override
    public GeneratedMessageV3 getPackagedMessage()
    {
        return message.getReqUnregMsg();
    }
}
