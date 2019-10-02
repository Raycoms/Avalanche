package com.ray.pbft;

import com.google.protobuf.GeneratedMessageV3;
import com.ray.mcu.communication.IMessageHandler;
import com.ray.mcu.communication.wrappers.IMessageWrapper;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.Server;
import io.netty.channel.ChannelHandlerContext;

/**
 * The Pbft specific message handler registry.
 */
public class PbftMessageHandlerRegistry
{
    /*
     * General todo list
     *
     * todo Add preprepare
     * todo Add prepare
     * todo Add commit
     *
     * Add recover current consensus data (case of lost preprepare)
     * Add recover past consensus results
     */

    /*
     * Register additional messages here.
     */
    static
    {
        //MessageHandlerRegistry.handlers.add()
    }

    /**
     * The classical handler of the preprepare message.
     * todo create proto message
     * todo create wrapper
     * todo add to queue
     * todo add verification
     *
     * Only valid coordinator can send it.
     * Has to contain a valid view.
     * (If view contains less -> Requires signed unregister to have passed previously) (Actual unregister only happens after included in view).
     * (If view contains more -> We "decide" to accept it).
     * (If view id = current view id + 1, if bigger than try to recover the signed last consensus results)
     * (If server received already a valid preprepare for this view)
     * (If client transactions are valid (enough money and correct sig)
     * (If already received one for the current round of consensus)
     *
     * If valid results in broadcasting prepare message.
     */
    public class PrePrepareMessageHandler implements IMessageHandler
    {

        @Override
        public void wrap(final MessageProto.Message message, final ChannelHandlerContext ctx, final Server server, final int sender)
        {

        }

        @Override
        public void handle(final IMessageWrapper message, final Server server)
        {

        }

        @Override
        public boolean canHandle(final MessageProto.Message message)
        {
            return message.hasPrePrepare();
        }

        @Override
        public GeneratedMessageV3 getMessage(final MessageProto.Message message)
        {
            return message.getPrePrepare();
        }
    }

}
