package com.raidzero.teamcitydownloader.global;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by raidzero on 7/17/14.
 */
public class TeamCityXmlParser {
    private static final String tag = "TeamCityXmlParser";

    private Document doc;

    public TeamCityXmlParser(String xml) {
        // parse this xml string into a Document
        this.doc = parseXmlString(xml);
    }

    private Document parseXmlString(String xml) {
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            return db.parse(is);
        } catch (Exception e) {
            Debug.Log(tag, "Document error: ", e);
        }

        return null;
    }

    public NodeList getNodes(String elementName) {
        NodeList nodes = doc.getElementsByTagName(elementName);
        Debug.Log(tag, "getNodes(" + elementName + ") returning " + nodes.getLength() + " nodes.");
        return nodes;
    }

    public NodeList getNodes(Element element, String elementName) {
        NodeList nodes = element.getElementsByTagName(elementName);
        Debug.Log(tag, "getNodes(" + elementName + ") returning " + nodes.getLength() + " nodes.");
        return nodes;
    }

    public String getAttribute(Element element, String attribute) {
        return element.getAttribute(attribute);
    }


}
