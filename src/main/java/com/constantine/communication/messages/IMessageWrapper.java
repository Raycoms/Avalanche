package com.constantine.communication.messages;

import com.constantine.nettyhandlers.SizedMessage;
import com.constantine.proto.MessageProto;
import com.constantine.server.IServer;

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
     * @param serverSender the sending server reference.
     * @return the ready SizedMessage.
     */
    public SizedMessage writeToSizedMessage(final IServer serverSender);

    /**
     * Build the byte array for the message.
     * @return the byte array to send it.
     */
    public byte[] buildMessage();

    /**
     * Get the message from the wrapper.
     * @return the message.
     */
    public MessageProto.Message getMessage();
}
