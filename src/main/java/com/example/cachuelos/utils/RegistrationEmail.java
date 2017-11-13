package com.example.cachuelos.utils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class RegistrationEmail {

	/**
	 * This method send the email from specific user to the registered email id
	 * 
	 * @author come2niks
	 * @param register
	 */
	static class EmailAuth extends Authenticator {

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			System.out.println("emailAuth");
			return new PasswordAuthentication("cachuelos.pe@gmail.com",
					"BEAbenito1");

		}
	}

	public static void sendRegistrationMail(String secretkey, String email, String host) {

		try {
			System.out.println("email al Inicio");
			Properties props = new Properties();
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.auth", "true");
			props.put("mail.debug", "false");
			props.put("mail.smtp.ssl.enable", "true");

			Session session = Session.getInstance(props, new EmailAuth());
			Message msg = new MimeMessage(session);

			InternetAddress from = new InternetAddress(
					"cachuelos.pe@gmail.com", "Cachuelos Cachuelos");
			msg.setFrom(from);

			InternetAddress toAddress = new InternetAddress(email);

			msg.setRecipient(Message.RecipientType.TO, toAddress);

			msg.setSubject("Bienvenido a la comunidad");

			msg.setContent("<html>\n" + "<body>\n" + "\n" +

			//"<a href='http://cachuelos-cachuelos.rhcloud.com/validateRegister.do?secretkey="
			"<a href='http://"+host+"/validateRegister.do?secretkey="
					+ secretkey + "'> Selecciona este enlace para activar tu cuenta" + "</a>" + "\n"
					+ "Gracias" + "</body>\n" + "</html>", "text/html");

			Transport.send(msg);
			System.out.println("emailFin");
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			System.out.println("error");

		} catch (MessagingException ex) {
			ex.printStackTrace();
			System.out.println("error");
		}

	}
}
