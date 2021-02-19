package com.projekt.projekt.Requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendEmailRequest {
    private String[] address;
    private String subject;
    private String content;
    private MultipartFile  [] attachments;

    public SendEmailRequest(String[] address, String subject,  MultipartFile[] attachments) {
        this.address = address;
        this.subject = subject;
        this.attachments = attachments;
    }

    public MultipartFile  [] getAttachments() {
        return attachments;
    }

    public void setAttachments(MultipartFile  [] attachments) {
        this.attachments = attachments;
    }

    public String[] getAddress() {
        return address;
    }

    public void setAddress(String[] address) {
        this.address = address;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
