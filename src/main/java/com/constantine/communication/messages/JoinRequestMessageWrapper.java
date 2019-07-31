package com.constantine.communication.messages;

import com.constantine.communication.handlers.SizedMessage;
import com.constantine.proto.MessageProto;
import com.constantine.server.ServerData;

/**
 * Example join request message wrapper.
 */
public class JoinRequestMessageWrapper implements IMessageWrapper
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
     * Create an instance of the join request message wrapper.
     * @param message the int to send.
     */
    public JoinRequestMessageWrapper(final ServerData message, final int sender)
    {
        this.message = message;
        this.sender = sender;
    }

    /**
     * Create an instance of the join request message wrapper.
     * @param message the message to extract it from.
     */
    public JoinRequestMessageWrapper(final MessageProto.RequestRegisterMessage message, final int sender)
    {
        this.message = new ServerData(message.getId(), message.getIp(), message.getPort());
        this.sender = sender;
    }

    @Override
    public SizedMessage writeToSizedMessage()
    {
        final MessageProto.RequestRegisterMessage.Builder intBuilder = MessageProto.RequestRegisterMessage.newBuilder();
        builder.setReqRegMsg(intBuilder.setId(this.message.getId()).setIp(message.getIp()).setPort(message.getPort()).build());

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
