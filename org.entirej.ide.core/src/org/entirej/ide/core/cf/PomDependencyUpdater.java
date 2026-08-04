package org.entirej.ide.core.cf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

final class PomDependencyUpdater
{
    private PomDependencyUpdater()
    {
    }

    static Result ensureDependency(byte[] pomSource, String groupId, String artifactId, String version, String scope)
            throws IOException
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setExpandEntityReferences(false);
            factory.setXIncludeAware(false);

            var builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            Document document = builder.parse(new ByteArrayInputStream(pomSource));
            Element project = document.getDocumentElement();
            if (!"project".equals(localName(project)))
            {
                throw new IOException("The project pom.xml has no Maven project root element.");
            }

            Element dependencies = directChild(project, "dependencies");
            if (dependencies == null)
            {
                dependencies = document.createElementNS(project.getNamespaceURI(), "dependencies");
                project.appendChild(document.createTextNode(System.lineSeparator() + "    "));
                project.appendChild(dependencies);
                project.appendChild(document.createTextNode(System.lineSeparator()));
            }

            for (Node child = dependencies.getFirstChild(); child != null; child = child.getNextSibling())
            {
                if (child instanceof Element dependency && "dependency".equals(localName(dependency))
                        && groupId.equals(directChildText(dependency, "groupId"))
                        && artifactId.equals(directChildText(dependency, "artifactId")))
                {
                    return new Result(pomSource, false);
                }
            }

            Element dependency = document.createElementNS(project.getNamespaceURI(), "dependency");
            appendTextElement(document, dependency, "groupId", groupId);
            appendTextElement(document, dependency, "artifactId", artifactId);
            if (version != null && !version.isBlank())
            {
                appendTextElement(document, dependency, "version", version);
            }
            if (scope != null && !scope.isBlank())
            {
                appendTextElement(document, dependency, "scope", scope);
            }
            dependency.appendChild(document.createTextNode(System.lineSeparator() + "        "));

            Node trailingWhitespace = trailingWhitespace(dependencies);
            Node insertionPoint = trailingWhitespace == null ? null : trailingWhitespace;
            dependencies.insertBefore(document.createTextNode(System.lineSeparator() + "        "), insertionPoint);
            dependencies.insertBefore(dependency, insertionPoint);
            if (trailingWhitespace == null)
            {
                dependencies.appendChild(document.createTextNode(System.lineSeparator() + "    "));
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(document), new StreamResult(output));
            return new Result(output.toByteArray(), true);
        }
        catch (ParserConfigurationException | SAXException | TransformerException e)
        {
            throw new IOException("Unable to update the project pom.xml.", e);
        }
    }

    private static void appendTextElement(Document document, Element parent, String name, String value)
    {
        parent.appendChild(document.createTextNode(System.lineSeparator() + "            "));
        Element child = document.createElementNS(parent.getNamespaceURI(), name);
        child.setTextContent(value);
        parent.appendChild(child);
    }

    private static Node trailingWhitespace(Element element)
    {
        Node lastChild = element.getLastChild();
        if (lastChild != null && lastChild.getNodeType() == Node.TEXT_NODE && lastChild.getTextContent().trim().isEmpty())
        {
            return lastChild;
        }
        return null;
    }

    private static String directChildText(Element parent, String name)
    {
        Element child = directChild(parent, name);
        return child == null ? null : child.getTextContent().trim();
    }

    private static Element directChild(Element parent, String name)
    {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if (child instanceof Element element && name.equals(localName(element)))
            {
                return element;
            }
        }
        return null;
    }

    private static String localName(Node node)
    {
        return node.getLocalName() == null ? node.getNodeName() : node.getLocalName();
    }

    static final class Result
    {
        private final byte[]  _content;
        private final boolean _changed;

        Result(byte[] content, boolean changed)
        {
            _content = content;
            _changed = changed;
        }

        byte[] content()
        {
            return _content;
        }

        boolean changed()
        {
            return _changed;
        }
    }
}
