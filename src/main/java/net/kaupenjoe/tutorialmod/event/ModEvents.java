package net.kaupenjoe.tutorialmod.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import net.kaupenjoe.tutorialmod.item.ModItems;
import net.kaupenjoe.tutorialmod.villager.ModVillagers;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID)
public class ModEvents {

    private static final String XES_FILE_PATH = "./player_events_log.xes";
    private static boolean isFileInitialized = false;
    private static final Map<String, StringBuilder> playerTraces = new HashMap<>();

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        if(event.getType() == VillagerProfession.FARMER) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            // Level 1
            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 2),
                    new ItemStack(ModItems.STRAWBERRY.get(), 12),
                    10, 8, 0.02f));

            // Level 2
            trades.get(2).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 5),
                    new ItemStack(ModItems.CORN.get(), 6),
                    5, 9, 0.035f));

            // Level 3
            trades.get(3).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.GOLD_INGOT, 8),
                    new ItemStack(ModItems.CORN_SEEDS.get(), 2),
                    2, 12, 0.075f));
        }

        if(event.getType() == VillagerProfession.LIBRARIAN) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();
            ItemStack enchantedBook = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(Enchantments.THORNS, 2));

            // Level 1
            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 32),
                    enchantedBook,
                    2, 8, 0.02f));
        }

        if(event.getType() == ModVillagers.SOUND_MASTER.get()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            trades.get(1).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 16),
                    new ItemStack(ModBlocks.SOUND_BLOCK.get(), 1),
                    16, 8, 0.02f));

            trades.get(2).add((pTrader, pRandom) -> new MerchantOffer(
                    new ItemStack(Items.EMERALD, 6),
                    new ItemStack(ModBlocks.SAPPHIRE_ORE.get(), 2),
                    5, 12, 0.02f));
        }
    }

    @SubscribeEvent
    public static void addCustomWanderingTrades(WandererTradesEvent event) {
        List<VillagerTrades.ItemListing> genericTrades = event.getGenericTrades();
        List<VillagerTrades.ItemListing> rareTrades = event.getRareTrades();

        genericTrades.add((pTrader, pRandom) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 12),
                new ItemStack(ModItems.SAPPHIRE_BOOTS.get(), 1),
                3, 2, 0.2f));

        rareTrades.add((pTrader, pRandom) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, 24),
                new ItemStack(ModItems.METAL_DETECTOR.get(), 1),
                2, 12, 0.15f));
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        System.out.println("/////////////////////////////////");
        System.out.println("/////////////////////////////////");

        Player player = event.getPlayer();
        Block block = event.getState().getBlock();

        // Track tree cutting
        if (block == Blocks.JUNGLE_LOG || block == Blocks.SPRUCE_LOG || block == Blocks.BIRCH_LOG || block == Blocks.OAK_LOG) {
            logEvent(player, "Cut down a tree");
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack craftedItem = event.getCrafting();

        // Track pickaxe crafting
        if (craftedItem.getItem() == Items.WOODEN_PICKAXE || craftedItem.getItem() == Items.STONE_PICKAXE ||
                craftedItem.getItem() == Items.IRON_PICKAXE || craftedItem.getItem() == Items.DIAMOND_PICKAXE) {
            logEvent(player, "Crafted a pickaxe");
        }
    }

    static {
        initializeXESFile();
    }
    // Initialize the XES log file with headers if it doesn’t exist
    private static void initializeXESFile() {
        try {
            if (!Files.exists(Paths.get(XES_FILE_PATH))) {
                // File does not exist, so create it with the opening log tag
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(XES_FILE_PATH))) {
                    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
                    writer.write("<log xes.version=\"1.0\" xes.features=\"nested-attributes\">\n");
                }
            } else {
                // File exists, so check for a closing </log> tag
                List<String> lines = Files.readAllLines(Paths.get(XES_FILE_PATH));
                if (!lines.isEmpty() && lines.get(lines.size() - 1).trim().equals("</log>")) {
                    // Remove </log> to allow new events to be added
                    lines.remove(lines.size() - 1);
                    Files.write(Paths.get(XES_FILE_PATH), lines);
                }
            }
            isFileInitialized = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void logEvent(Player player, String action) {
//        LocalDateTime now = LocalDateTime.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
//
//        String timestamp = now.format(formatter);
//
//        String playerId = player.getUUID().toString();
//        String playerName = player.getName().getString();
//
//        // Write event to the XES log file (New trace for every event)
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(XES_FILE_PATH, true))) {
//            // Start a new trace for each player (you may need more sophisticated logic to avoid duplicates)
//            writer.write("  <trace>\n");
//            writer.write("    <string key=\"concept:name\" value=\"" + playerId + "\"/>\n");
//
//            // Add an event to the trace
//            writer.write("    <event>\n");
//            writer.write("      <string key=\"concept:name\" value=\"" + action + "\"/>\n");
//            writer.write("      <string key=\"org:resource\" value=\"" + playerName + "\"/>\n");
//            writer.write("      <date key=\"time:timestamp\" value=\"" + timestamp + "\"/>\n");
//            writer.write("    </event>\n");
//            writer.write("  </trace>\n");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        if (!isFileInitialized) {
            initializeXESFile();
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String timestamp = now.format(formatter);

        String playerId = player.getUUID().toString();
        String playerName = player.getName().getString();

        // Get or create the trace for the player
        playerTraces.putIfAbsent(playerId, new StringBuilder()
                .append("  <trace>\n")
                .append("    <string key=\"concept:name\" value=\"").append(playerId).append("\"/>\n"));

        StringBuilder trace = playerTraces.get(playerId);
        trace.append("    <event>\n")
                .append("      <string key=\"concept:name\" value=\"").append(action).append("\"/>\n")
                .append("      <string key=\"org:resource\" value=\"").append(playerName).append("\"/>\n")
                .append("      <date key=\"time:timestamp\" value=\"").append(timestamp).append("\"/>\n")
                .append("    </event>\n");
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        closeXESLog();
    }

    // Call this once to close the XES file when all logging is done, adding the closing </log> tag
    public static void closeXESLog() {
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(XES_FILE_PATH, true))) {
//            writer.write("</log>");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (!isFileInitialized) {
            initializeXESFile();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(XES_FILE_PATH, true))) {
            for (StringBuilder trace : playerTraces.values()) {
                if (!trace.toString().contains("</trace>")) {
                    trace.append("  </trace>\n");
                }
                writer.write(trace.toString());
            }
            writer.write("</log>");  // Only add </log> once at the very end
            System.out.println("XES log file closed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        isFileInitialized = false;
    }

}
