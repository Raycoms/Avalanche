package com.constantine.communication.messages;

import com.constantine.proto.MessageProto;
import com.constantine.server.IServer;
import com.constantine.server.ServerData;
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
        super(sender.getServerData().getId(), builder.setRegMsg(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
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
    public RegisterMessageWrapper(final int sender, final MessageProto.Message message)
    {
        super(sender, message);
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
        final MessageProto.RegisterMessage msg = this.message.getRegMsg();
        return new ServerData(msg.getId(), msg.getIp(), msg.getPort());
    }
}
