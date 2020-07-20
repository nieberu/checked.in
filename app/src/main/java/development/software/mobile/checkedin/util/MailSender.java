package development.software.mobile.checkedin.util;

import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import development.software.mobile.checkedin.models.Group;
import development.software.mobile.checkedin.models.User;

public class MailSender extends javax.mail.Authenticator {
    private String mailhost = "smtp.gmail.com"; //Hostname of the SMTP mail server which you want to connect for sending emails.
    private String user;
    private String password;
    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public MailSender(String user, String password) {
        this.user = user; //Your SMTP username. In case of GMail SMTP this is going to be your GMail email address.
        this.password = password; //Your SMTP password. In case of GMail SMTP this is going to be your GMail password.

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public synchronized void sendMail(String sender, String recipients, Group group, User user) throws Exception {

        String subject = "You have been invited to a group!";
        String body = "<h1 style=\"text-align: center;\">Hello, "+ user.getFirstName() + " " + user.getLastName() + " has invited you to their group " + group.getName()+"!</h1>\n" +
                "  <p style=\"text-align: center;\">Use the following information to join the group in the app\n" +
                "    <br/>\n" +
                "    <br/>\n" +
                "    Group Name: "+group.getName()+"\n" +
                "    <br/>\n" +
                "    <br/>\n" +
                "    Key: "+group.getKey()+"\n" +
                "    <br/>\n" +
                "    <br/>\n" +
                "    Group owner email: "+group.getOwner()+"\n" +
                "  </p>\n" +
                "  <h2 style=\"text-align: center;\">Thank you for using Checked.In!!</h2>";
        MimeMessage message = new MimeMessage(session);
        DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/html"));
        message.setSender(new InternetAddress(sender));
        message.setSubject(subject);
        message.setDataHandler(handler);

        if (recipients.indexOf(',') > 0)
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
        else
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));

        Transport.send(message);
    }
}