package com.ipn.mx.service;

import java.io.ByteArrayInputStream;
public interface EmailService {
    void sendSimpleMessage(String to, String subject, String text);
    void sendMessageWithAttachment(String to, String subject, String text, ByteArrayInputStream attachmentData, String attachmentName);
}
