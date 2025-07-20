import org.w3c.dom.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

public class PropertiesLoaderUtil {

    private static boolean modified = false;

    public static Properties load(String xmlPath, String secretKey) {
        Properties props = new Properties();
        try {
            File file = Paths.get(xmlPath).toFile();
            if (!file.exists()) throw new FileNotFoundException("Config file not found at: " + xmlPath);

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList entries = doc.getElementsByTagName("entry");

            for (int i = 0; i < entries.getLength(); i++) {
                Element entry = (Element) entries.item(i);
                String key = entry.getAttribute("key");

                if (key.equals("mail.smtp.username") || key.equals("mail.smtp.password")) {
                    String value = getTextContentAndMaybeEncrypt(entry, secretKey, true);
                    props.put(key, value);
                } else {
                    props.put(key, entry.getTextContent());
                }
            }

            saveXmlIfModified(doc, file);
        } catch (Exception e) {
            throw new RuntimeException("Error loading secure mail properties: " + e.getMessage(), e);
        }

        return props;
    }

    private static String getTextContentAndMaybeEncrypt(Element element, String key, boolean doEncrypt) {
        String state = element.getAttribute("text");
        String value = element.getTextContent();

        if ("plain/text".equalsIgnoreCase(state) && doEncrypt) {
            String encrypted = CredentialSecurityUtil.encrypt(value, key);
            element.setTextContent(encrypted);
            element.setAttribute("text", "ENCRYPTED");
            modified = true;
            return value;
        } else if ("ENCRYPTED".equalsIgnoreCase(state) && doEncrypt) {
            return CredentialSecurityUtil.decrypt(value, key);
        } else {
            return value;
        }
    }

    private static void saveXmlIfModified(Document doc, File file) throws TransformerException {
        if (!modified) return;
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.transform(new DOMSource(doc), new StreamResult(file));
        System.out.println("🔒 Updated config with encrypted credentials.");
    }
}
