package com.constantine.communication.messages;

import com.constantine.proto.MessageProto;
import com.constantine.server.IServer;
import com.constantine.server.ServerData;
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
        super(sender.getServerData().getId(), builder.setReqUnregMsg(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
    }

    /**
     * Create an instance of the unregister request message wrapper.
     * @param message the int to send.
     */
    public UnregisterRequestMessageWrapper(final IServer sender, final ServerData message)
    {
        this(sender, MessageProto.RequestUnregisterMessage.newBuilder().setId(message.getId()).setIp(message.getIp()).setPort(message.getPort()).build());
    }

    @Override
    public byte[] buildMessage(final IServer serverSender)
    {
        return this.message.toByteArray();
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
}
