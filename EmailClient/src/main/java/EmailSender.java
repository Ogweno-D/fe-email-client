import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.io.*;
import java.util.Properties;

public class EmailSender {
    public static void main(String[] args) throws Exception {
        String secretKey = EnvLoader.getSecretKey();
        String configPath = EnvLoader.getConfigPath();
        Properties props = PropertiesLoaderUtil.load(configPath,secretKey);

        // Load the credentials
        final String username = props.getProperty("mail.smtp.username");
        final String password = props.getProperty("mail.smtp.password");

        String fromEmail = "support@tatua.go.ke";
        String toEmail = "jadeogweno@gmail.com";

        InputStream imageStream = EmailSender.class.getClassLoader().getResourceAsStream("assets/tatua-logo.png");
        if (imageStream == null) throw new FileNotFoundException("Image not found");
        byte[] imageBytes = imageStream.readAllBytes();

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            TemplateEngine templateEngine = templateEngine();
            Context context = new Context();
            context.setVariable("firstName", "William");
            context.setVariable("setupLink", "https://www.google.com");
            // String html = templateEngine.process("welcome-email.html", context);
            // String html = templateEngine.process("trial-expiration.html", context);
            String html = templateEngine.process("product-update-newsletter.html", context);


            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "Tatua Support"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            // message.setSubject("Welcome to Tatua - Let's Get Started");
            // message.setSubject("Your Tatua Trial Expires Tomorrow - Don't Lose Your Data ");
            message.setSubject(" New Features: AI- Powered Ticket Routing & More ");

            MimeBodyPart htmlBodyPart = new MimeBodyPart();
            htmlBodyPart.setContent(html, "text/html; charset=utf-8");

            MimeBodyPart imageBodyPart = new MimeBodyPart();
            imageBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(imageBytes, "image/png")));
            imageBodyPart.setHeader("Content-ID", "logoImage");
            imageBodyPart.setDisposition(MimeBodyPart.INLINE);

            MimeMultipart multipart = new MimeMultipart("related");
            multipart.addBodyPart(htmlBodyPart);
            multipart.addBodyPart(imageBodyPart);

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Email sent successfully");

        } catch (Exception e) {
            e.printStackTrace(
                    System.out.printf("Error: %s%n", e.getMessage())

            );
        }
    }

    private static String getHtmlEmailTemplate() {
            try (InputStream is = EmailSender.class.getClassLoader().getResourceAsStream("templates/welcome-email.html")) {
                if (is == null) {
                    throw new FileNotFoundException("Email template not found in classpath: templates/welcome-email.html");
                }
                return new String(is.readAllBytes());
            } catch (IOException e) {
            e.printStackTrace(
                    System.out.printf("Error: %s%n", e.getMessage())
            );
            return "";
        }
    }

    private static TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
        return engine;
    }
}
