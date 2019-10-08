package com.ray.pbft.communication.wrappers;

import com.google.protobuf.ByteString;
import com.ray.mcu.communication.wrappers.AbstractMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.ray.mcu.utils.Log;

/**
 * Wrapper for the Commit Message.
 */
public class RecoverCommitWrapper extends AbstractMessageWrapper
{
    /**
     * Wrap an existing commit message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public RecoverCommitWrapper(final int sender, final MessageProto.Message message)
    {
        super(sender, message);
    }

    /**
     * Wrap a commit message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public RecoverCommitWrapper(final IServer sender, final MessageProto.Commit message)
    {
        this(sender.getServerData().getId(), MessageProto.Message.newBuilder().setCommit(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
    }

    /**
     * Create a new wrapper from the previous preprepare.
     * @param sender the sender.
     * @param prepare the preprare message.
     */
    public static RecoverCommitWrapper createCommitWrapper(final IServer sender, final PrepareWrapper[] prepare)
    {
        if (prepare.length == 0)
        {
            Log.getLogger().error("Fatal error when trying to create commit wrapper with an empty array of prepare messages.");
            return null;
        }

        final MessageProto.Commit.Builder builder = MessageProto.Commit.newBuilder();
        builder.setInputHash(prepare[0].getMessage().getPrepare().getInputHash()).setView(prepare[0].getMessage().getPrepare().getView());

        for (int i = 0; i < prepare.length; i++)
        {
            builder.setSignatures(i, MessageProto.Signature.newBuilder().setId(prepare[i].sender).setSig(prepare[i].getMessage().getSig()).build());
        }

        return new RecoverCommitWrapper(sender, builder.build());
    }
}
