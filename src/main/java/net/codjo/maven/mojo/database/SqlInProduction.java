package net.codjo.maven.mojo.database;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
/**
 *
 */
public class SqlInProduction {
    private NodeList extractTags(File changesfile, String tagName)
          throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory documentBuilderFactory =
              DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(changesfile);
        Element root = document.getDocumentElement();

        return root.getElementsByTagName(tagName);
    }


    public String getVersionFromSources(String changesFilePath)
          throws IOException, ParserConfigurationException, SAXException {
        NodeList releaseTags = extractTags(new File(changesFilePath), "release");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String leastVersionDate = null;
        String leastVersionNumber = null;
        for (int i = 0; i < releaseTags.getLength(); i++) {
            Element releaseTag = (Element)releaseTags.item(i);
            String date = releaseTag.getAttribute("date");
            String version = releaseTag.getAttribute("version");
            String inProduction = releaseTag.getAttribute("in_production");
            try {
                if ("true".equals(inProduction)) {
                    if (leastVersionDate == null) {
                        leastVersionDate = date;
                        leastVersionNumber = version;
                    }
                    if (simpleDateFormat.parse(date).after(simpleDateFormat.parse(
                          leastVersionDate))) {
                        leastVersionNumber = version;
                        leastVersionDate = date;
                    }
                }
            }
            catch (ParseException dateParseException) {
                throw new ParserConfigurationException(
                      "Incorect Date format in xml document :" + dateParseException);
            }
        }
        return leastVersionNumber;
    }


    public String getVersionFromDeployedApplication(String jnlpFilePath)
          throws IOException, ParserConfigurationException, SAXException, JnlpBadFormatException {
        NodeList jarTags = extractTags(new File(jnlpFilePath), "jar");
        String href = null;
        for (int i = 0; i < jarTags.getLength(); i++) {
            Element jarTag = (Element)jarTags.item(i);
            String main = jarTag.getAttribute("main");
            if ("true".equals(main)) {
                if (href != null) {
                    throw new JnlpBadFormatException(
                          "Many 'jar' tag found with 'main' attribute");
                }
                href = jarTag.getAttribute("href");
                String j2eeName = "ClientAPP-";
                int beginIndex = j2eeName.length();
                String notJ2eeName = "-client-";
                if (href.indexOf(notJ2eeName) > 0) {
                    beginIndex = href.indexOf(notJ2eeName) + notJ2eeName.length();
                }
                int endIndex = href.lastIndexOf(".jar");
                href = href.substring(beginIndex, endIndex);
            }
        }
        if (href == null) {
            throw new JnlpBadFormatException("No 'jar' tag found with 'main' attribute");
        }
        return href;
    }


    /**
     * Exception lancee quand le JNLP parsé est invalide.
     */
    public static class JnlpBadFormatException extends Exception {
        public JnlpBadFormatException(String message) {
            super(message);
        }
    }
}
