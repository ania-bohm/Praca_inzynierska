package com.annabohm.pracainzynierska;

import java.util.Date;

public class Message {
    private String messageId;
    private String messageContent;
    private Date createdAt;
    private String messageSenderName;

    public Message(String messageId, String messageContent, Date createdAt, String messageSenderName) {
        this.messageId = messageId;
        this.messageContent = messageContent;
        this.createdAt = createdAt;
        this.messageSenderName = messageSenderName;
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
}
