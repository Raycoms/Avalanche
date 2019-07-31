package com.constantine.communication.messages;

import com.constantine.communication.handlers.SizedMessage;
import com.constantine.proto.MessageProto;
import com.constantine.server.ServerData;

/**
 * Example register Message.
 */
public class RegisterMessageWrapper implements IMessageWrapper
{
    /**
     * The String of this message.
     */
    private final ServerData message;

    /**
     * Id of the sender.
     */
    public final int sender;

    /**
     * Create an instance of the register message.
     * @param message the server to register.
     * @param sender the sender.
     */
    public RegisterMessageWrapper(final ServerData message, final int sender)
    {
        this.message = message;
        this.sender = sender;
    }

    /**
     * Create an instance of the register message wrapper.
     * @param message the message to extract it from.
     * @param sender the sender.
     */
    public RegisterMessageWrapper(final MessageProto.RegisterMessage message, final int sender)
    {
        this.message = new ServerData(message.getId(), message.getIp(), message.getPort());
        this.sender = sender;
    }

    /**
     * Create an instance of the register message wrapper.
     * @param message the join request resulting in the register.
     */
    public RegisterMessageWrapper(final JoinRequestMessageWrapper message)
    {
        this.message = message.getServerData();
        this.sender = message.sender;
    }

    @Override
    public SizedMessage writeToSizedMessage()
    {
        final MessageProto.RegisterMessage.Builder intBuilder = MessageProto.RegisterMessage.newBuilder();
        builder.setRegMsg(intBuilder.setId(this.message.getId()).setIp(message.getIp()).setPort(message.getPort()).build());

        return new SizedMessage(builder.build().toByteArray(), sender);
    }

    /**
     * Get the associated server data.
     * @return the server data.
     */
    public ServerData getServerData()
    {
        return message;
    }
}
