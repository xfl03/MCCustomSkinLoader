package customskinloader.bootstrap.util;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Small DOM helpers shared by mapping and patch XML parsers.
 */
public final class XmlUtils {
    private XmlUtils() {
    }

    public static Document parseDocument(Reader reader, String documentDescription) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);
            factory.setIgnoringComments(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(reader));
            document.getDocumentElement().normalize();
            return document;
        } catch (ParserConfigurationException exception) {
            throw new IOException("Failed to configure XML parser for " + documentDescription, exception);
        } catch (SAXException exception) {
            throw new IOException("Failed to parse " + documentDescription, exception);
        }
    }

    public static String requiredAttribute(Element element, String attributeName) {
        String value = element.getAttribute(attributeName);
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required attribute '" + attributeName + "' on <" + element.getTagName() + ">");
        }
        return value.trim();
    }

    public static List<Element> childElements(Element parentElement, String requiredTagName) {
        if (parentElement == null) {
            return Collections.emptyList();
        }

        List<Element> elements = new ArrayList<>();
        NodeList childNodes = parentElement.getChildNodes();
        for (int index = 0; index < childNodes.getLength(); index++) {
            Node currentNode = childNodes.item(index);
            if (currentNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element currentElement = (Element) currentNode;
            if (requiredTagName == null || requiredTagName.equals(currentElement.getTagName())) {
                elements.add(currentElement);
            }
        }
        return elements;
    }
}
