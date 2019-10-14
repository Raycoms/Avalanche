package com.ray.pbft.communication.wrappers;

import com.google.protobuf.ByteString;
import com.ray.mcu.communication.wrappers.AbstractMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.ray.mcu.server.Server;
import com.ray.mcu.server.ServerData;
import com.ray.mcu.utils.Log;
import com.ray.mcu.views.GlobalView;

import java.util.Arrays;
import java.util.List;

/**
 * Wrapper for the PrePrepare Message.
 */
public class PrePrepareWrapper extends AbstractMessageWrapper
{
    /**
     * Wrap an existing preprepare message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public PrePrepareWrapper(final int sender, final MessageProto.Message message)
    {
        super(sender, message);
    }

    /**
     * Wrap an existing preprepare message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public PrePrepareWrapper(final IServer sender, final MessageProto.PrePrepare message)
    {
        this(sender.getServerData().getId(), MessageProto.Message.newBuilder().setPrePrepare(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
    }

    /**
     * Factory method to construct an instance of the PrePrepareWrapper.
     *
     * @param view the view of the sender.
     * @param sender the server which will send it.
     * @param data the data to send.
     * @param inputHash the signature.
     * @return a filled instance of the PrePrepareWrapper.
     */
    public static PrePrepareWrapper createPrePrepareWrapper(
      final MessageProto.View view,
      final int sender,
      final List<MessageProto.PersistClientMessage> data,
      final ByteString inputHash)
    {
        final MessageProto.PrePrepare.Builder prePrepareBuilder = MessageProto.PrePrepare.newBuilder();
        prePrepareBuilder.setView(view);

        for (final MessageProto.PersistClientMessage message : data)
        {
            prePrepareBuilder.addInput(message);
        }

        return new PrePrepareWrapper(sender, MessageProto.Message.newBuilder().setPrePrepare(prePrepareBuilder.build()).setSig(inputHash).build());
    }

    /**
     * Factory method to construct an instance of the PrePrepareWrapper.
     *
     * @param sender the server which will send it.
     * @param data the data to send.
     * @return a filled instance of the PrePrepareWrapper.
     */
    public static PrePrepareWrapper createPrePrepareWrapper(final Server sender, final List<MessageProto.PersistClientMessage> data)
    {
        final MessageProto.PrePrepare.Builder prePrepareBuilder = MessageProto.PrePrepare.newBuilder();

        final GlobalView view = sender.getView();
        prePrepareBuilder.setView(view.processViewToProto());

        for (final MessageProto.PersistClientMessage message : data)
        {
            prePrepareBuilder.addInput(message);
        }

        return new PrePrepareWrapper(sender, prePrepareBuilder.build());
    }
}
