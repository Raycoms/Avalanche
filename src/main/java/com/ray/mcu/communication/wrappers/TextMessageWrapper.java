package com.ray.mcu.communication.wrappers;

import com.ray.mcu.proto.MessageProto;
import com.ray.mcu.server.IServer;
import com.google.protobuf.ByteString;

/**
 * Example String Message.
 */
public class TextMessageWrapper extends AbstractMessageWrapper
{
    /**
     * Create an instance of the string message wrapper.
     * @param message the message to send.
     * @param sender the sender.
     */
    public TextMessageWrapper(final IServer sender, final MessageProto.TextMessage message)
    {
        super(sender.getServerData().getId(), MessageProto.Message.newBuilder().setTextMsg(message).setSig(ByteString.copyFrom(sender.signMessage(message.toByteArray()))).build());
    }

    /**
     * Create an instance of the String message.
     * @param message the String to send.
     * @param sender the sender.
     */
    public TextMessageWrapper(final IServer sender, final String message)
    {
        this(sender, MessageProto.TextMessage.newBuilder().setText(message).build());
    }

    /**
     * Get the text from the text message.
     * @return the string.
     */
    public String getString()
    {
        return this.message.getTextMsg().getText();
    }
}
