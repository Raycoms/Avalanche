package com.ray.mcu.communication.wrappers;

import com.google.protobuf.GeneratedMessageV3;
import com.ray.mcu.nettyhandlers.SizedMessage;
import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;

/**
 * The Message Wrapper interface to easily create messages and move them around.
 */
public interface IMessageWrapper
{
    /**
     * Write the IMessageWrapper to the SizedMessage to send via Netty.
     * @param serverSender the sending server reference.
     * @return the ready SizedMessage.
     */
    public SizedMessage writeToSizedMessage(final IServer serverSender);

    /**
     * Build the byte array for the message.
     * @param serverSender the server sender.
     * @return the byte array to send it.
     */
    public byte[] buildMessage(final IServer serverSender);

    /**
     * Get the message from the wrapper.
     * @return the message.
     */
    public MessageProto.Message getMessage();

    /**
     * Get the supposed sender from the message.
     * @return the sender id.
     */
    public int getSender();

    /**
     * Get the packaged message.
     */
    public GeneratedMessageV3 getPackagedMessage();
}
