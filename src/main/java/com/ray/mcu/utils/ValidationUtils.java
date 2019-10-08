package com.ray.mcu.utils;

import com.ray.mcu.communication.wrappers.AbstractMessageWrapper;
import com.ray.mcu.communication.wrappers.IMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.Server;
import com.ray.mcu.views.GlobalView;
import com.ray.pbft.communication.wrappers.PrepareWrapper;
import com.ray.pbft.server.PbftServer;
import sun.security.rsa.RSAPublicKeyImpl;

import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.List;

/**
 * Utility class for verifications.
 */
public final class ValidationUtils
{
    public static boolean isMessageLogValid(final AbstractMessageWrapper message, final PbftServer server)
    {
        for (final MessageProto.PersistClientMessage data : message.getMessage().getPrePrepare().getInputList())
        {
            final MessageProto.ClientMessage msg = data.getMsg();

            try
            {
                final PublicKey key = new RSAPublicKeyImpl(msg.getPkey().toByteArray());
                if (!KeyUtilities.verifyKey(msg.toByteArray(), message.getMessage().getPersClientMsg().getSig().toByteArray(), key))
                {
                    Log.getLogger().warn("Invalid signature from client!");
                    return false;
                }

                int tempState = server.state.getOrDefault(key, 0);

                if (tempState + msg.getDif() < 0)
                {
                    Log.getLogger().warn("----------------------------------------------------------------\n"
                                           + "Transactions tried to debit invalid quantity! (" + message.getSender() + ")"
                                           + "\n----------------------------------------------------------------");
                    return false;
                }

                tempState += msg.getDif();

                Log.getLogger().warn("New State: " + tempState);
                server.state.put(key, tempState);
            }
            catch (InvalidKeyException e)
            {
                Log.getLogger().warn("----------------------------------------------------------------\n"
                                       + "Transactions included invalidly signed client transaction! (" + message.getSender() + ")"
                                       + "\n----------------------------------------------------------------");
                return false;
            }
        }
        return true;
    }

    /**
     * Verify the signatures of a commit message
     *
     * @param list   the signatures to verify.
     * @param server the server verifying this.
     * @return true if sufficient valid.
     */
    public static boolean verifyCommit(final List<MessageProto.Signature> list, final PbftServer server)
    {
        final int threshold = (server.view.getServers().size() / (3 * 2)) + 1;
        final byte[] msg = MessageProto.Prepare.newBuilder()
                             .setView(server.currentPrePrepare.getSecond().getMessage().getPrePrepare().getView())
                             .setInputHash(server.currentPrePrepare.getSecond().message.getSig())
                             .build()
                             .toByteArray();

        int validSigs = 0;
        for (final MessageProto.Signature data : list)
        {
            if (!KeyUtilities.verifyKey(msg, data.getSig().toByteArray(), server.getView().getServer(data.getId()).getPublicKey()))
            {
                Log.getLogger().warn("Invalid signature from client!");
                continue;
            }
            validSigs++;
        }

        return validSigs >= threshold;
    }
}
