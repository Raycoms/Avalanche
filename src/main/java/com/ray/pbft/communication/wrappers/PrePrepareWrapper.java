package com.ray.pbft.communication.wrappers;

import com.google.protobuf.ByteString;
import com.ray.mcu.communication.wrappers.AbstractMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.ray.mcu.server.ServerData;
import com.ray.mcu.views.GlobalView;

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
     * @param sender the server which will send it.
     * @param data the data to send.
     * @return a filled instance of the PrePrepareWrapper.
     */
    public static PrePrepareWrapper createPrePrepareWrapper(final IServer sender, final List<MessageProto.PersistClientMessage> data)
    {
        final MessageProto.PrePrepare.Builder prePrepareBuilder = MessageProto.PrePrepare.newBuilder();

        final GlobalView view = sender.getView();

        final MessageProto.View.Builder viewBuilder = MessageProto.View.newBuilder();
        viewBuilder.setCoordinator(view.getCoordinator());
        viewBuilder.setId(view.getId());

        int index = 0;
        for (final ServerData serverData : view.getServers())
        {
            viewBuilder.setServers(index++, MessageProto.Server.newBuilder().setId(serverData.getId()).setPort(serverData.getPort()).setIp(serverData.getIp()));
        }

        prePrepareBuilder.setView(viewBuilder.build());

        index = 0;
        for (final MessageProto.PersistClientMessage message : data)
        {
            prePrepareBuilder.setInput(index++, message);
        }

        return new PrePrepareWrapper(sender, prePrepareBuilder.build());
    }
}
