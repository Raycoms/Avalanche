package com.ray.pbft.communication.wrappers;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.ray.mcu.communication.wrappers.AbstractMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.ray.mcu.server.Server;
import com.ray.mcu.views.GlobalView;
import java.util.List;

/**
 * Wrapper for the PrePrepare Message.
 */
public class PrePrepareWrapper extends AbstractMessageWrapper
{
    /**
     * Retry of the PrePrepare
     */
    public int retry = 0;

    /**
     * Wrap an existing preprepare message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public PrePrepareWrapper(final int sender, final MessageProto.Message.Builder message)
    {
        super(sender, message);
    }

    /**
     * Wrap an existing preprepare message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public PrePrepareWrapper(final int sender, final MessageProto.Message.Builder message, final boolean signedAlready)
    {
        super(sender, message);
        this.alreadySigned = signedAlready;
    }

    /**
     * Wrap an existing preprepare message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public PrePrepareWrapper(final IServer sender, final MessageProto.PrePrepare message)
    {
        this(sender.getServerData().getId(), MessageProto.Message.newBuilder().setPrePrepare(message));
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

        return new PrePrepareWrapper(sender, MessageProto.Message.newBuilder().setPrePrepare(prePrepareBuilder.build()).setSig(inputHash));
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

    @Override
    public GeneratedMessageV3 getPackagedMessage()
    {
        return message.getPrePrepare();
    }
}
