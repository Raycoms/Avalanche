package com.ray.pbft.communication.wrappers;

import com.google.protobuf.ByteString;
import com.ray.mcu.communication.wrappers.AbstractMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.ray.mcu.utils.Log;

/**
 * Wrapper for the Commit Message.
 */
public class CommitWrapper extends AbstractMessageWrapper
{
    /**
     * Wrap an existing commit message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public CommitWrapper(final int sender, final MessageProto.Message message)
    {
        super(sender, message);
    }

    /**
     * Wrap a commit message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public CommitWrapper(final IServer sender, final MessageProto.Commit message)
    {
        this(sender.getServerData().getId(), MessageProto.Message.newBuilder().setCommit(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
    }

    /**
     * Create a new wrapper from the previous preprepare.
     * @param sender the sender.
     * @param prepare the preprare message.
     */
    public static CommitWrapper createCommitWrapper(final IServer sender, final PrepareWrapper[] prepare)
    {
        if (prepare.length == 0)
        {
            Log.getLogger().error("Fatal error when trying to create commit wrapper with an empty array of prepare messages.");
            return null;
        }

        final MessageProto.Commit.Builder builder = MessageProto.Commit.newBuilder();
        builder.setInputHash(prepare[0].getMessage().getPrepare().getInputHash()).setView(prepare[0].getMessage().getPrepare().getView());
        builder.setView(sender.getView().processViewToProto());

        for (final PrepareWrapper prepareWrapper : prepare)
        {
            builder.addSignatures(MessageProto.Signature.newBuilder().setId(prepareWrapper.sender).setSig(prepareWrapper.getMessage().getSig()).build());
        }

        return new CommitWrapper(sender, builder.build());
    }
}
