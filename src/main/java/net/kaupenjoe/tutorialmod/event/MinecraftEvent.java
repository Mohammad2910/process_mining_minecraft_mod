package net.kaupenjoe.tutorialmod.event;

import net.minecraft.world.entity.player.Player;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Event data structure to hold all event information
public class MinecraftEvent {
    private static int nextCaseId = 1;

    final String eventType;        // e.g., "combat", "crafting", "mining"
    final String activity;         // specific action e.g., "KillMob:Zombie"
    final Player player;           // the player performing the action
    final Map<String, String> attributes;  // additional event-specific attributes
    final LocalDateTime timestamp;
    final int caseId;             // Unique identifier for each event

    public MinecraftEvent(String eventType, String activity, Player player) {
        this.eventType = eventType;
        this.activity = activity;
        this.player = player;
        this.attributes = new HashMap<>();
        this.timestamp = LocalDateTime.now();
        this.caseId = nextCaseId++;
    }

    public MinecraftEvent addAttribute(String key, String value) {
        if (value != null) {
            this.attributes.put(key, value);
        }
        return this;
    }
}

