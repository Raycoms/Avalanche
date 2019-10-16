package com.ray.mcu.communication.wrappers;

import com.google.protobuf.GeneratedMessageV3;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.ray.mcu.server.ServerData;
import com.google.protobuf.ByteString;

/**
 * Example register Message.
 */
public class UnregisterMessageWrapper extends AbstractMessageWrapper
{
    /**
     * Create an instance of the unregister message wrapper.
     *
     * @param message the message to extract it from.
     * @param sender  the sender.
     */
    public UnregisterMessageWrapper(final IServer sender, final MessageProto.UnregisterMessage message)
    {
        super(sender.getServerData().getId(), MessageProto.Message.newBuilder().setUnregMsg(message));
    }

    /**
     * Create an instance of the register message.
     *
     * @param server the server to register.
     * @param sender the sender.
     * @param sig    the signature proofing that the replica wants to unregister.
     */
    public UnregisterMessageWrapper(final IServer sender, final ServerData server, final byte[] sig)
    {
        this(sender,
          MessageProto.UnregisterMessage.newBuilder().setId(server.getId()).setIp(server.getIp()).setPort(server.getPort()).setSignature(ByteString.copyFrom(sig)).build());
        this.alreadySigned = true;
    }

    /**
     * Create an instance of the register message wrapper.
     *
     * @param sender  the sender.
     * @param message the join request resulting in the register.
     */
    public UnregisterMessageWrapper(final IServer sender, final MessageProto.Message.Builder message)
    {
        this(sender,
          MessageProto.UnregisterMessage.newBuilder()
            .setId(message.getReqUnregMsg().getId())
            .setIp(message.getReqUnregMsg().getIp())
            .setPort(message.getReqUnregMsg().getPort())
            .setSignature(message.getSig())
            .build());
        this.alreadySigned = true;
    }

    /**
     * Create an instance of the unregister message wrapper.
     *
     * @param message the message to extract it from.
     * @param sender  the sender.
     */
    public UnregisterMessageWrapper(final int sender, final MessageProto.Message.Builder message)
    {
        super(sender, message);
    }

    /**
     * Get the associated server data.
     *
     * @return the server data.
     */
    public ServerData getServerData()
    {
        final MessageProto.UnregisterMessage msg = this.message.getUnregMsg();
        return new ServerData(msg.getId(), msg.getIp(), msg.getPort());
    }

    /**
     * Get the signature.
     *
     * @return the signature.
     */
    public byte[] getSig()
    {
        final MessageProto.UnregisterMessage msg = this.message.getUnregMsg();
        return msg.getSignature().toByteArray();
    }

    @Override
    public GeneratedMessageV3 getPackagedMessage()
    {
        return message.getUnregMsg();
    }

    @Override
    public byte[] buildMessage(final IServer serverSender)
    {
        if (this.alreadySigned)
        {
            return message.build().toByteArray();
        }
        return super.buildMessage(serverSender);
    }
}
