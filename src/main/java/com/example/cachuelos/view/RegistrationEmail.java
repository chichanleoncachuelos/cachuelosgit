package com.example.cachuelos.view;


import java.io.Serializable;

import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;

/**
 * <p>
 * {@link Email} contains all the business logic for the application, and also serves as the controller for the JSF view.
 * </p>
 * <p>
 * It contains address, subject, and content for the <code>email</code> to be sent.
 * </p>
 * <p>
 * The {@link #send()} method provides the business logic to send the email
 * </p>
 * 
 * @author Joel Tosi
 * 
 */

@Named
@SessionScoped
public class RegistrationEmail implements Serializable {

    private static final long serialVersionUID = 1544680932114626710L;

    /**
     * Resource for sending the email. The mail subsystem is defined in either standalone.xml or domain.xml in your respective
     * configuration directory.
     */
    
    @Resource(name = "java:jboss/mail/gmail")
    private Session mySession;

//    private String to;
//
//    private String from;
//
//    private String subject;
//
//    private String body;
//
//    public String getTo() {
//        return to;
//    }
//
//    public void setTo(String to) {
//        this.to = to;
//    }
//
//    public String getFrom() {
//        return from;
//    }
//
//    public void setFrom(String from) {
//        this.from = from;
//    }
//
//    public String getSubject() {
//        return subject;
//    }
//
//    public void setSubject(String subject) {
//        this.subject = subject;
//    }
//
//    public String getBody() {
//        return body;
//    }
//
//    public void setBody(String body) {
//        this.body = body;
//    }

    /**
     * Method to send the email based upon values entered in the JSF view. Exception should be handled in a production usage but
     * is not handled in this example.
     * 
     * @throws Exception
     */
    public RegistrationEmail() {
    }
    
    public  void send(String secretkey, String to, String host) throws Exception {
    	System.out.println("email session: " +mySession);
//    	mySession = InitialContext.doLookup("java:jboss/mail/gmail");
//    	System.out.println("email session2: " +mySession);
    	System.out.println("email al Inicio");
        Message message = new MimeMessage(mySession);
        Address toAddress = new InternetAddress(to);
        message.addRecipient(Message.RecipientType.TO, toAddress);
        message.setSubject("Bienvenido al sistema de empleos cortos");
        message.setContent("<html>\n" + "<body>\n" + "\n" +

			//"<a href='http://cachuelos-cachuelos.rhcloud.com/validateRegister.do?secretkey="
			"<a href='http://"+host+"/validateRegister.do?secretkey="
					+ secretkey + "'> Selecciona este enlace para activar tu cuenta" + "</a>" + "\n"
					+ "Gracias" + "</body>\n" + "</html>", "text/html");
        Transport.send(message);
        System.out.println("email al Final");
    }
    
//    public static void sendRegistrationMail(String secretkey, String email, String host) {
//
//		try {
//			System.out.println("email al Inicio");
//			Properties props = new Properties();
//			props.put("mail.smtp.host", "smtp.gmail.com");
//			props.put("mail.smtp.auth", "true");
//			props.put("mail.debug", "false");
//			props.put("mail.smtp.ssl.enable", "true");
//
//			Session session = Session.getInstance(props, new EmailAuth());
//			Message msg = new MimeMessage(session);
//
//			InternetAddress from = new InternetAddress(
//					"cachuelos.pe@gmail.com", "Cachuelos Cachuelos");
//			msg.setFrom(from);
//
//			InternetAddress toAddress = new InternetAddress(email);
//
//			msg.setRecipient(Message.RecipientType.TO, toAddress);
//
//			msg.setSubject("Bienvenido a la comunidad");
//
//			msg.setContent("<html>\n" + "<body>\n" + "\n" +
//
//			//"<a href='http://cachuelos-cachuelos.rhcloud.com/validateRegister.do?secretkey="
//			"<a href='http://"+host+"/validateRegister.do?secretkey="
//					+ secretkey + "'> Selecciona este enlace para activar tu cuenta" + "</a>" + "\n"
//					+ "Gracias" + "</body>\n" + "</html>", "text/html");
//
//			Transport.send(msg);
//			System.out.println("emailFin");
//		} catch (UnsupportedEncodingException ex) {
//			ex.printStackTrace();
//			System.out.println("error");
//
//		} catch (MessagingException ex) {
//			ex.printStackTrace();
//			System.out.println("error");
//		}
//
//	}
}
