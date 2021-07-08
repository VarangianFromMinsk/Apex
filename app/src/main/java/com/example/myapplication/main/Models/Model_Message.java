package com.example.myapplication.main.Models;

public class Model_Message {

    private String text;
    private String name;
    private String imageUrl;
    private String sender;  // Отправитель
    private String recipient;  // Получатель  ( может будть 1 или несколько)
    private String timeOfMessage;
    private String dayOfMessage;
    private String recipientImageAvatar;
    private String isThatRepost;
    private String postId;
    private String isThatRecord;
    private String recordUrl;
    private String keyMessage;
    private boolean isMine;

    public Model_Message(){     // Конструктор
    }

    public Model_Message(String text, String name, String imageUrl, String sender, String recipient, String timeOfMessage, String dayOfMessage, String recipientImageAvatar, String isThatRepost, String postId, String isThatRecord, String recordUrl, String keyMessage, boolean isMine) {
        this.text = text;
        this.name = name;
        this.imageUrl = imageUrl;
        this.sender = sender;
        this.recipient = recipient;
        this.timeOfMessage = timeOfMessage;
        this.dayOfMessage = dayOfMessage;
        this.recipientImageAvatar = recipientImageAvatar;
        this.isThatRepost = isThatRepost;
        this.postId = postId;
        this.isThatRecord = isThatRecord;
        this.recordUrl = recordUrl;
        this.keyMessage = keyMessage;
        this.isMine = isMine;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean mine) {
        isMine = mine;
    }

    public String getTimeOfMessage() {
        return timeOfMessage;
    }

    public void setTimeOfMessage(String timeOfMessage) {
        this.timeOfMessage = timeOfMessage;
    }

    public String getRecipientImageAvatar() {
        return recipientImageAvatar;
    }

    public void setRecipientImageAvatar(String recipientImageAvatar) {
        this.recipientImageAvatar = recipientImageAvatar;
    }

    public String getDayOfMessage() {
        return dayOfMessage;
    }

    public void setDayOfMessage(String dayOfMessage) {
        this.dayOfMessage = dayOfMessage;
    }

    public String getIsThatRepost() {
        return isThatRepost;
    }

    public void setIsThatRepost(String isThatRepost) {
        this.isThatRepost = isThatRepost;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getRecordUrl() {
        return recordUrl;
    }

    public void setRecordUrl(String recordUrl) {
        this.recordUrl = recordUrl;
    }

    public String getIsThatRecord() {
        return isThatRecord;
    }

    public void setIsThatRecord(String isThatRecord) {
        this.isThatRecord = isThatRecord;
    }

    public String getKeyMessage() {
        return keyMessage;
    }

    public void setKeyMessage(String keyMessage) {
        this.keyMessage = keyMessage;
    }
}
