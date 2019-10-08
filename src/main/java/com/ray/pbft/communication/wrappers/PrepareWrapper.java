package com.ray.pbft.communication.wrappers;

import com.google.protobuf.ByteString;
import com.ray.mcu.communication.wrappers.AbstractMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.ray.mcu.server.ServerData;
import com.ray.mcu.views.GlobalView;

import java.util.List;

/**
 * Wrapper for the Prepare Message.
 */
public class PrepareWrapper extends AbstractMessageWrapper
{
    /**
     * Wrap an existing prepare message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public PrepareWrapper(final int sender, final MessageProto.Message message)
    {
        super(sender, message);
    }

    /**
     * Wrap a prepare message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public PrepareWrapper(final IServer sender, final MessageProto.Prepare message)
    {
        this(sender.getServerData().getId(), MessageProto.Message.newBuilder().setPrepare(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
    }

    /**
     * Create a new wrapper from the previous preprepare.
     * @param sender the sender.
     * @param prePrepare the preprare message.
     */
    public PrepareWrapper(final IServer sender, final MessageProto.Message prePrepare)
    {
        this(sender, MessageProto.Prepare.newBuilder().setView(prePrepare.getPrePrepare().getView()).setInputHash(prePrepare.getSig()).build());
    }
}
