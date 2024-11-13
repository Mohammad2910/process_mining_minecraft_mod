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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
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
    public static void onMobKilled(LivingDeathEvent event) {
        // Add debug print
        System.out.println("LivingDeathEvent triggered!");

        if (!(event.getSource().getEntity() instanceof Player player)) {
            System.out.println("Not killed by player, source was: " + event.getSource().getEntity());
            return;
        }

        String entityName = event.getEntity().getName().getString();
        System.out.println("Player " + player.getName().getString() + " killed " + entityName);

        // Use new MinecraftEvent class
        MinecraftEvent mobKillEvent = new MinecraftEvent(
                "combat",
                "KillMob:" + entityName,
                player
        );

        // Add attributes
        mobKillEvent
                .addAttribute("combat:target", entityName)
                .addAttribute("combat:weapon",
                        player.getMainHandItem().isEmpty() ? "none" :
                                player.getMainHandItem().getDisplayName().getString())
                .addAttribute("combat:location", String.format("%.1f,%.1f,%.1f",
                        player.getX(), player.getY(), player.getZ()))
                .addAttribute("combat:dimension",
                        player.level().dimension().location().toString());

        // Log using EventLogger
        EventLogger.logEvent(mobKillEvent);
        System.out.println("Event logged successfully");
    }


    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getState().getBlock();

        // Track tree cutting
        if (block == Blocks.JUNGLE_LOG || block == Blocks.SPRUCE_LOG ||
                block == Blocks.BIRCH_LOG || block == Blocks.OAK_LOG) {

            MinecraftEvent treeEvent = new MinecraftEvent(
                    "harvesting",
                    "CutTree:" + block.getName().getString(),
                    player
            );

            treeEvent
                    .addAttribute("harvesting:block", block.getName().getString())
                    .addAttribute("harvesting:tool",
                            player.getMainHandItem().isEmpty() ? "none" :
                                    player.getMainHandItem().getDisplayName().getString())
                    .addAttribute("harvesting:location", String.format("%.1f,%.1f,%.1f",
                            player.getX(), player.getY(), player.getZ()));

            EventLogger.logEvent(treeEvent);
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack craftedItem = event.getCrafting();

        // Track pickaxe crafting
        if (craftedItem.getItem() == Items.WOODEN_PICKAXE ||
                craftedItem.getItem() == Items.STONE_PICKAXE ||
                craftedItem.getItem() == Items.IRON_PICKAXE ||
                craftedItem.getItem() == Items.DIAMOND_PICKAXE) {

            MinecraftEvent craftEvent = new MinecraftEvent(
                    "crafting",
                    "CraftTool:Pickaxe",
                    player
            );

            craftEvent
                    .addAttribute("crafting:item", craftedItem.getDisplayName().getString())
                    .addAttribute("crafting:material", craftedItem.getItem().toString().split("_")[0])
                    .addAttribute("crafting:quantity", String.valueOf(craftedItem.getCount()));

            EventLogger.logEvent(craftEvent);
        }
    }

    // Add back the ServerStopping event handler
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        System.out.println("Server stopping, closing log file...");
        EventLogger.closeLog();
    }



}
