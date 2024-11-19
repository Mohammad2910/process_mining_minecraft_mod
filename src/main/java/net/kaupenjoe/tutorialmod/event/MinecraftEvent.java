package net.kaupenjoe.tutorialmod.event;

import net.minecraft.world.entity.player.Player;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

public class MinecraftEvent {
    private static int nextCaseId = 1;

    final String eventType;
    final String activity;
    final Player player;
    final Map<String, String> attributes;
    final int caseId;
    public String worldId;
    final LocalDateTime timestamp;

    public MinecraftEvent(String eventType, String activity, Player player) {
        this.eventType = eventType;
        this.activity = activity;
        this.player = player;
        this.attributes = new HashMap<>();
        this.timestamp = LocalDateTime.now(ZoneOffset.UTC);
        this.caseId = nextCaseId++;
    }

    public String getPlayerId() {
        return player != null ? player.getUUID().toString() : "system";
    }

    public String getPlayerName() {
        return player != null ? player.getName().getString() : "system";
    }

    public MinecraftEvent addAttribute(String key, String value) {
        if (value != null) {
            this.attributes.put(key, value);
        }
        return this;
    }
}
