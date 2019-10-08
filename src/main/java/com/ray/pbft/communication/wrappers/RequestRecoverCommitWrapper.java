package com.ray.pbft.communication.wrappers;

import com.google.protobuf.ByteString;
import com.ray.mcu.communication.wrappers.AbstractMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;

/**
 * Wrapper for the Request Recover Commit Message.
 */
public class RequestRecoverCommitWrapper extends AbstractMessageWrapper
{
    /**
     * Wrap an existing Request Recover Commit message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public RequestRecoverCommitWrapper(final int sender, final MessageProto.Message message)
    {
        super(sender, message);
    }

    /**
     * Wrap an existing Request Recover Commit message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public RequestRecoverCommitWrapper(final IServer sender, final MessageProto.RequestRecoverCommit message)
    {
        this(sender.getServerData().getId(), MessageProto.Message.newBuilder().setRequestRecoverCommit(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
    }

    /**
     * Create a new Request Recover Commit.
     *
     * @param sender the sender.
     * @param viewId the viewId to recover.
     */
    public RequestRecoverCommitWrapper(final IServer sender, final int viewId)
    {
        this(sender, MessageProto.RequestRecoverCommit.newBuilder().setViewId(viewId).build());
    }
}
