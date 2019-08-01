package com.constantine.communication.messages;

import com.constantine.communication.handlers.SizedMessage;
import com.constantine.proto.MessageProto;
import com.constantine.server.ServerData;
import com.google.protobuf.ByteString;

/**
 * Example unregister request message wrapper.
 */
public class UnregisterRequestMessageWrapper implements IMessageWrapper
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
     * Create an instance of the unregister request message wrapper.
     * @param message the int to send.
     */
    public UnregisterRequestMessageWrapper(final ServerData message, final int sender)
    {
        this.message = message;
        this.sender = sender;
    }

    /**
     * Create an instance of the unregister request message wrapper.
     * @param message the message to extract it from.
     */
    public UnregisterRequestMessageWrapper(final MessageProto.RequestUnregisterMessage message, final int sender)
    {
        this.message = new ServerData(message.getId(), message.getIp(), message.getPort());
        this.sender = sender;
    }

    @Override
    public SizedMessage writeToSizedMessage()
    {
        final MessageProto.RequestUnregisterMessage.Builder intBuilder = MessageProto.RequestUnregisterMessage.newBuilder();
        builder.setReqUnregMsg(intBuilder.setId(this.message.getId()).setIp(message.getIp()).setPort(message.getPort()).build()).setSignature(ByteString.copyFrom(new byte[0]));

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
