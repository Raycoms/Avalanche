package com.constantine.communication.messages;

import com.constantine.communication.handlers.SizedMessage;
import com.constantine.proto.MessageProto;

/**
 * The Message Wrapper interface to easily create messages and move them around.
 */
public interface IMessageWrapper
{
    /**
     * The proto message builder to avoid creating a new builder for each message.
     */
    static final MessageProto.Message.Builder builder = MessageProto.Message.newBuilder();

    /**
     * Write the IMessageWrapper to the SizedMessage to send via Netty.
     * @return the ready SizedMessage.
     */
    public SizedMessage writeToSizedMessage();
}
