package com.soapui;

import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class JavaMail {

	public static String sendMail(String from, String passwordString, String toList, String ccList, String subject, String body, String filename) throws MessagingException {
		String status = null;
		String host = "b19edns001.sagamino.ibm.com";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.from", from);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", "25");
		final String username = from;
		final String password = passwordString;
		// Get the Session object.
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		

			Message message = new MimeMessage(session);
			BodyPart contenet = new MimeBodyPart();
			contenet.setText(body);
			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));
			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toList));
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccList));
			// Set Subject: header field
			message.setSubject(subject);
			Multipart multipart = new MimeMultipart();
			String[] fileList = filename.split(",");
			for (String string : fileList) {
				addAttachment(multipart, string);
			}
			multipart.addBodyPart(contenet);
			message.setContent(multipart);
			// Send message
			Transport.send(message);
			status = "Success";
		

		return status;
	}

	private static void addAttachment(Multipart multipart, String filename) {
		DataSource source = new FileDataSource(filename);
		BodyPart messageBodyPart = new MimeBodyPart();
		try {
			messageBodyPart.setFileName(filename);
			multipart.addBodyPart(messageBodyPart);

			messageBodyPart.setDataHandler(new DataHandler(source));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
