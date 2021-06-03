package com.annabohm.pracainzynierska;

import java.util.Date;

public class Message {
    private String messageId;
    private String messageContent;
    private Date createdAt;
    private String messageSenderName;
    private String messageSenderId;

    public Message() {

    }

    public Message(String messageId, String messageContent, Date createdAt, String messageSenderName, String messageSenderId) {
        this.messageId = messageId;
        this.messageContent = messageContent;
        this.createdAt = createdAt;
        this.messageSenderName = messageSenderName;
        this.messageSenderId = messageSenderId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessageSenderName() {
        return messageSenderName;
    }

    public void setMessageSenderName(String messageSenderName) {
        this.messageSenderName = messageSenderName;
    }

    public String getMessageSenderId() {
        return messageSenderId;
    }

    public void setMessageSenderId(String messageSenderId) {
        this.messageSenderId = messageSenderId;
    }
}
