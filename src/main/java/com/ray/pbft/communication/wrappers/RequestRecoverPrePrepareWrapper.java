package com.ray.pbft.communication.wrappers;

import com.google.protobuf.GeneratedMessageV3;
import com.ray.mcu.communication.wrappers.AbstractMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;

/**
 * Wrapper for the Request Recover PrePrepare Message.
 */
public class RequestRecoverPrePrepareWrapper extends AbstractMessageWrapper
{
    /**
     * Wrap an existing Request Recover PrePrepare message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public RequestRecoverPrePrepareWrapper(final int sender, final MessageProto.Message.Builder message)
    {
        super(sender, message);
    }

    /**
     * Wrap an existing Request Recover PrePrepare message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public RequestRecoverPrePrepareWrapper(final IServer sender, final MessageProto.RequestRecoverPrePrepare message)
    {
        this(sender.getServerData().getId(), MessageProto.Message.newBuilder().setRequestRecoverPrePrepare(message));
    }

    /**
     * Create a new Request Recover PrePrepare.
     *
     * @param sender the sender.
     * @param viewId the viewId to recover.
     */
    public RequestRecoverPrePrepareWrapper(final IServer sender, final int viewId)
    {
        this(sender, MessageProto.RequestRecoverPrePrepare.newBuilder().setViewId(viewId).build());
    }

    @Override
    public GeneratedMessageV3 getPackagedMessage()
    {
        return message.getRequestRecoverPrePrepare();
    }
}
