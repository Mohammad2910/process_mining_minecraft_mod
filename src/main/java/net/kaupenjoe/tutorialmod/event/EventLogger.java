package net.kaupenjoe.tutorialmod.event;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class EventLogger {
    private static final String XES_FILE_PATH = "./minecraft_process_mining.xes";
    private static Document document;
    private static Element rootElement;
    private static boolean isInitialized = false;
    private static final Map<String, Element> traceElements = new HashMap<>();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private static void initializeIfNeeded() {
        if (!isInitialized) {
            try {
                File file = new File(XES_FILE_PATH);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setIgnoringElementContentWhitespace(true);
                DocumentBuilder builder = factory.newDocumentBuilder();

                if (file.exists()) {
                    document = builder.parse(file);
                    document.normalizeDocument();
                    rootElement = document.getDocumentElement();

                    NodeList traces = document.getElementsByTagName("trace");
                    for (int i = 0; i < traces.getLength(); i++) {
                        Element trace = (Element) traces.item(i);
                        NodeList strings = trace.getElementsByTagName("string");
                        for (int j = 0; j < strings.getLength(); j++) {
                            Element string = (Element) strings.item(j);
                            if ("concept:name".equals(string.getAttribute("key"))) {
                                traceElements.put(string.getAttribute("value"), trace);
                                break;
                            }
                        }
                    }
                } else {
                    document = builder.newDocument();
                    rootElement = document.createElement("log");
                    rootElement.setAttribute("xes.version", "1.0");
                    rootElement.setAttribute("xes.features", "nested-attributes");
                    document.appendChild(rootElement);
                }

                isInitialized = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void logEvent(MinecraftEvent event) {
        initializeIfNeeded();

        String worldName = event.player.level().dimension().location().getPath();
        String playerId = event.getPlayerId();
        String traceId = worldName + "_" + playerId;

        Element trace = traceElements.computeIfAbsent(traceId, k -> createNewTrace(traceId, event));
        appendEvent(trace, event);
        saveDocument();
    }

    private static Element createNewTrace(String traceId, MinecraftEvent event) {
        Element trace = document.createElement("trace");

        addStringElement(trace, "concept:name", traceId);
        addStringElement(trace, "player:name", event.getPlayerName());
        addStringElement(trace, "world:name",
                event.player.level().dimension().location().getPath());
        addStringElement(trace, "description", "Process instance for player in world");

        rootElement.appendChild(trace);
        return trace;
    }

    private static void appendEvent(Element trace, MinecraftEvent event) {
        Element eventElement = document.createElement("event");

        addStringElement(eventElement, "concept:name", event.activity);
        addStringElement(eventElement, "org:resource", event.getPlayerName());
        addStringElement(eventElement, "time:timestamp",
                event.timestamp.format(TIMESTAMP_FORMATTER));
        addStringElement(eventElement, "event:type", event.eventType);
        addStringElement(eventElement, "lifecycle:transition", "complete");
        addStringElement(eventElement, "case:id", String.valueOf(event.caseId));

        for (Map.Entry<String, String> attr : event.attributes.entrySet()) {
            addStringElement(eventElement, attr.getKey(), attr.getValue());
        }

        trace.appendChild(eventElement);
    }

    private static void addStringElement(Element parent, String key, String value) {
        Element element = document.createElement("string");
        element.setAttribute("key", key);
        element.setAttribute("value", value);
        parent.appendChild(element);
    }

    private static void saveDocument() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Minimize whitespace while keeping basic formatting
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            // Remove extra whitespace
            document.normalizeDocument();
            removeWhitespace(document.getDocumentElement());

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(XES_FILE_PATH));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void removeWhitespace(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                if (child.getTextContent().trim().isEmpty()) {
                    node.removeChild(child);
                }
            } else {
                removeWhitespace(child);
            }
        }
    }

    public static void closeLog() {
        isInitialized = false;
        traceElements.clear();
    }
}