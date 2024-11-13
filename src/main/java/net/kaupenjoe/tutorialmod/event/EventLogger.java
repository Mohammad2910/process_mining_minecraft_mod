package net.kaupenjoe.tutorialmod.event;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Main event logger class
public class EventLogger {
    private static final String XES_FILE_PATH = "./minecraft_process_mining.xes";
    private static boolean isFileInitialized = false;
    private static final Map<String, StringBuilder> playerTraces = new HashMap<>();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public static void logEvent(MinecraftEvent event) {
        initializeIfNeeded();

        String playerId = event.player.getUUID().toString();
        String playerName = event.player.getName().getString();
        String timestamp = event.timestamp.format(TIMESTAMP_FORMATTER);

        StringBuilder trace = playerTraces.computeIfAbsent(playerId, k -> createNewTrace(playerId, playerName));

        appendEvent(trace, event, timestamp);
    }

    private static StringBuilder createNewTrace(String playerId, String playerName) {
        return new StringBuilder()
                .append("  <trace>\n")
                .append("    <string key=\"concept:name\" value=\"").append(playerId).append("\"/>\n")
                .append("    <string key=\"player:name\" value=\"").append(playerName).append("\"/>\n")
                .append("    <string key=\"description\" value=\"Complete process instance for player\"/>\n");
    }

    private static void appendEvent(StringBuilder trace, MinecraftEvent event, String timestamp) {
        trace.append("    <event>\n")
                .append("      <string key=\"concept:name\" value=\"").append(event.activity).append("\"/>\n")
                .append("      <string key=\"org:resource\" value=\"").append(event.player.getName().getString()).append("\"/>\n")
                .append("      <date key=\"time:timestamp\" value=\"").append(timestamp).append("\"/>\n")
                .append("      <string key=\"event:type\" value=\"").append(event.eventType).append("\"/>\n")
                .append("      <string key=\"lifecycle:transition\" value=\"complete\"/>\n")
                .append("      <string key=\"case:id\" value=\"").append(event.caseId).append("\"/>\n");

        for (Map.Entry<String, String> attr : event.attributes.entrySet()) {
            trace.append("      <string key=\"").append(attr.getKey())
                    .append("\" value=\"").append(attr.getValue()).append("\"/>\n");
        }

        trace.append("    </event>\n");
    }

    public static void closeLog() {
        if (!isFileInitialized) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(XES_FILE_PATH))) {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            writer.write("<log xes.version=\"1.0\" xes.features=\"nested-attributes\">\n");

            for (StringBuilder trace : playerTraces.values()) {
                writer.write(trace.toString());
                writer.write("  </trace>\n");
            }

            writer.write("</log>");
            System.out.println("XES log file closed successfully.");

            playerTraces.clear();
            isFileInitialized = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeIfNeeded() {
        if (!isFileInitialized) {
            isFileInitialized = true;
            playerTraces.clear();
        }
    }
}