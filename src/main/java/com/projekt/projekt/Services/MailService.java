package com.projekt.projekt.Services;

import com.projekt.projekt.Requests.SendEmailRequest;
import com.sun.istack.ByteArrayDataSource;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@Service
public class MailService {
    final private JavaMailSender emailSender;
    final private Configuration configuration;

    public MailService(JavaMailSender emailSender, Configuration configuration) {
        this.emailSender = emailSender;
        this.configuration = configuration;
    }

    public String sendEmail(SendEmailRequest request, Map<String, Object> model, String template) {
        try {
            if (request.getAddress().length == 0) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please enter atleast one email address!");
            }

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Template t = configuration.getTemplate(template);
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);

            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(html, "UTF-8", "html");

            helper.setTo(request.getAddress());
            helper.setSubject((request.getSubject() == null ? "" : request.getSubject()));
            helper.setText(html, true);

            if (request.getAttachments() != null) {
                setAttachments(request, message, textBodyPart);
            }
            new Thread(() -> {
                emailSender.send(message);
            }).start();

        } catch (MessagingException | IOException | TemplateException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while sending email");
        }
        return "Sucessfully sent!";
    }

    public void setAttachments(SendEmailRequest request, MimeMessage message, MimeBodyPart textBodyPart) {
        try {
            Integer totalFilesSize = Arrays.stream(request.getAttachments()).mapToInt(i -> (int) i.getSize()).sum();
            if (totalFilesSize > 25000000) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Files cannot be larger than 25MB.");
            }

            Multipart emailContent = new MimeMultipart();

            for (MultipartFile xd : request.getAttachments()) {
                MimeBodyPart jpgBodyPart = new MimeBodyPart();
                DataSource ds = new ByteArrayDataSource(xd.getBytes(), xd.getContentType());
                jpgBodyPart.setDataHandler(new DataHandler(ds));
                jpgBodyPart.setFileName(xd.getOriginalFilename());
                jpgBodyPart.setDisposition(Part.ATTACHMENT);
                emailContent.addBodyPart(jpgBodyPart);
                message.setContent(emailContent);
            }

            emailContent.addBodyPart(textBodyPart);
        } catch (MessagingException | IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while sending email");
        }
    }
}
