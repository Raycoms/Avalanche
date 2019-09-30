package com.constantine.communication.messages;

import com.constantine.proto.MessageProto;
import com.constantine.server.IServer;
import com.constantine.server.ServerData;
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
        super(sender.getServerData().getId(), builder.setReqRegMsg(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
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

    @Override
    public byte[] buildMessage()
    {
        return this.message.toByteArray();
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
}
