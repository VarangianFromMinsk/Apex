package com.example.myapplication.main.Models;

public class Model_Start_Writing {
    String sender, recipient, value;

    public Model_Start_Writing() {
    }

    public Model_Start_Writing(String sender, String recipient, String value) {
        this.sender = sender;
        this.recipient = recipient;
        this.value = value;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
