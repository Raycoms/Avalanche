package com.ray.pbft.communication.wrappers;

import com.google.protobuf.GeneratedMessageV3;
import com.ray.mcu.communication.wrappers.AbstractMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.ray.mcu.utils.Log;
import com.ray.pbft.server.PbftServer;

import java.util.List;

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
    public RecoverCommitWrapper(final int sender, final MessageProto.Message.Builder message)
    {
        super(sender, message);
    }

    /**
     * Wrap a commit message.
     *
     * @param sender  the sender.
     * @param message the message.
     */
    public RecoverCommitWrapper(final IServer sender, final MessageProto.RecoverCommit message)
    {
        this(sender.getServerData().getId(), MessageProto.Message.newBuilder().setRecoverCommit(message));
    }

    /**
     * Create a new wrapper to recover the commits..
     * @param sender the sender.
     * @param commit the list of commits to send.
     */
    public static RecoverCommitWrapper createCommitWrapper(final IServer sender, final List<CommitWrapper> commit)
    {
        if (commit.isEmpty())
        {
            Log.getLogger().error("Fatal error when trying to create commit wrapper with an empty array of prepare messages.");
            return null;
        }

        final MessageProto.RecoverCommit.Builder builder = MessageProto.RecoverCommit.newBuilder();

        for (CommitWrapper commitWrapper : commit)
        {
            final MessageProto.CommitStorage.Builder storage = MessageProto.CommitStorage.newBuilder();
            storage.setInputHash(commitWrapper.message.getCommit().getInputHash());
            storage.setView(commitWrapper.message.getCommit().getView());
            for (int j = 0; j < commitWrapper.message.getCommit().getSignaturesCount(); j++)
            {
                storage.addSignatures(j, commitWrapper.message.getCommit().getSignatures(j));
            }
            final PrePrepareWrapper wrapper = ((PbftServer) sender).getPrePrepareForId(commitWrapper.message.getCommit().getView().getId());

            if (wrapper == null)
            {
                Log.getLogger().error("Fatal error when trying to get pre-prepare for commit.");
                return null;
            }

            for (int k = 0; k < wrapper.getMessage().getPrePrepare().getInputCount(); k++)
            {
                storage.addInput(wrapper.getMessage().getPrePrepare().getInput(k));
            }

            builder.addCommits(storage.build());
        }

        return new RecoverCommitWrapper(sender, builder.build());
    }

    @Override
    public GeneratedMessageV3 getPackagedMessage()
    {
        return message.getRecoverCommit();
    }
}
