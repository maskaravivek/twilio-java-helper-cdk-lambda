package com.maskaravivek;

public class SendSmsRequest {
    private String phoneNumber;
    private String message;

    public SendSmsRequest(String phoneNumber, String message) {
        this.phoneNumber = phoneNumber;
        this.message = message;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getMessage() {
        return message;
    }
}
