package com.constantine.communication.messages;

import com.constantine.proto.MessageProto;
import com.constantine.server.IServer;
import com.constantine.server.ServerData;
import com.google.protobuf.ByteString;

/**
 * Example register Message.
 */
public class UnregisterMessageWrapper extends AbstractMessageWrapper
{
    /**
     * Create an instance of the unregister message wrapper.
     * @param message the message to extract it from.
     * @param sender the sender.
     */
    public UnregisterMessageWrapper(final IServer sender, final MessageProto.UnregisterMessage message)
    {
        super(sender.getServerData().getId(), builder.setUnregMsg(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
    }

    /**
     * Create an instance of the register message.
     * @param server the server to register.
     * @param sender the sender.
     * @param sig the signature proofing that the replica wants to unregister.
     */
    public UnregisterMessageWrapper(final IServer sender, final ServerData server, final byte[] sig)
    {
        this(sender, MessageProto.UnregisterMessage.newBuilder().setId(server.getId()).setIp(server.getIp()).setPort(server.getPort()).setSignature(ByteString.copyFrom(sig)).build());
    }

    /**
     * Create an instance of the register message wrapper.
     * @param sender the sender.
     * @param message the join request resulting in the register.
     */
    public UnregisterMessageWrapper(final IServer sender, final MessageProto.Message message)
    {
        this(sender, MessageProto.UnregisterMessage.newBuilder().setId(message.getReqUnregMsg().getId()).setIp(message.getReqUnregMsg().getIp()).setPort(message.getReqUnregMsg().getPort()).setSignature(message.getSig()).build());
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
        final MessageProto.UnregisterMessage msg = this.message.getUnregMsg();
        return new ServerData(msg.getId(), msg.getIp(), msg.getPort());
    }

    /**
     * Get the signature.
     * @return the signature.
     */
    public byte[] getSig()
    {
        final MessageProto.UnregisterMessage msg = this.message.getUnregMsg();
        return msg.getSignature().toByteArray();
    }
}
