package com.constantine.communication.messages;

import com.constantine.communication.handlers.SizedMessage;
import com.constantine.proto.MessageProto;
import com.google.protobuf.ByteString;

/**
 * Example String Message.
 */
public class TextMessageWrapper implements IMessageWrapper
{
    /**
     * The String of this message.
     */
    private final String message;

    /**
     * Id of the sender.
     */
    public final int sender;

    /**
     * Create an instance of the String message.
     * @param message the String to send.
     * @param sender the sender.

     */
    public TextMessageWrapper(final String message, final int sender)
    {
        this.message = message;
        this.sender = sender;
    }

    /**
     * Create an instance of the String message.
     * @param message the Text message to create it from.
     * @param sender the sender.
     */
    public TextMessageWrapper(final MessageProto.TextMessage message, final int sender)
    {
        this.message = message.getText();
        this.sender = sender;
    }

    @Override
    public SizedMessage writeToSizedMessage()
    {
        final MessageProto.TextMessage.Builder intBuilder = MessageProto.TextMessage.newBuilder();
        builder.setTextMsg(intBuilder.setText(this.message).build()).setSignature(ByteString.copyFrom(new byte[0]));
        return new SizedMessage(builder.build().toByteArray(), sender);
    }
}
