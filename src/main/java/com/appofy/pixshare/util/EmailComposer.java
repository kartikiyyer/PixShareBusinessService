package com.appofy.pixshare.util;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailComposer {	
	
	private final String username;
	private final String password;
	private final String fromEmail;
	
	public EmailComposer(){
		username = "pixshare.contact@gmail.com";
		password = "rohantan";
		fromEmail = "pixshare.contact@gmail.com";
	}
	
	public void sendEMail(String toEmail, String emailsubject, String emailText){
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });
 
		try {
 
			Message message = new MimeMessage(session);
			message.setHeader("Content-Type", "text/html");
			message.setFrom(new InternetAddress(fromEmail));
			message.setRecipients(Message.RecipientType.TO,
			InternetAddress.parse(toEmail));
			message.setSubject(emailsubject);
			message.setText(emailText);

			Transport.send(message);
 
			System.out.println("email sent...");
 
		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EmailComposer emailComposer = new EmailComposer();
		emailComposer.sendEMail("", "", "");
	}
}
