package net.kaupenjoe.tutorialmod.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import net.kaupenjoe.tutorialmod.item.ModItems;
import net.kaupenjoe.tutorialmod.villager.ModVillagers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.*;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
        ItemStack tool = player.getMainHandItem();

        System.out.println("This is the tool item " + tool);
        String toolType = "none";
        if (!tool.isEmpty()) {
            Item item = tool.getItem();
            if (item instanceof PickaxeItem) toolType = "pickaxe";
            else if (item instanceof AxeItem) toolType = "axe";
            else if (item instanceof ShovelItem) toolType = "shovel";
            else if (item instanceof HoeItem) toolType = "hoe";
            else if (item instanceof SwordItem) toolType = "sword";
            else toolType = "other";
        }

        MinecraftEvent blockEvent = new MinecraftEvent(
                "mining",
                "Break:" + block.getName().getString(),
                player
        );

        blockEvent
                .addAttribute("mining:block", block.getName().getString())
                .addAttribute("mining:tool_type", toolType)
                .addAttribute("mining:tool_name", tool.isEmpty() ? "none" :
                        tool.getDisplayName().getString())
                .addAttribute("mining:location", String.format("%.1f,%.1f,%.1f",
                        player.getX(), player.getY(), player.getZ()))
                .addAttribute("mining:dimension", player.level().dimension().location().toString())
                .addAttribute("mining:exp_drop", String.valueOf(event.getExpToDrop()))
                .addAttribute("mining:depth", String.valueOf((int)player.getY()))
                .addAttribute("mining:block_hardness", String.valueOf(
                        block.defaultBlockState().getDestroySpeed(player.level(), event.getPos())))
                .addAttribute("mining:can_harvest", String.valueOf(
                        tool.isCorrectToolForDrops(block.defaultBlockState())));

        // Add damage information for tools
        if (!tool.isEmpty() && tool.isDamageableItem()) {
            blockEvent
                    .addAttribute("mining:tool_durability", String.valueOf(tool.getMaxDamage() - tool.getDamageValue()))
                    .addAttribute("mining:tool_max_durability", String.valueOf(tool.getMaxDamage()));
        }

        EventLogger.logEvent(blockEvent);
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        ItemStack craftedItem = event.getCrafting();
        Item item = craftedItem.getItem();

        // Determine item category based on class hierarchy
        String category = "unknown";
        if (item instanceof ArmorItem) category = "armor";
        else if (item instanceof BlockItem) category = "block";
        else if (item instanceof TieredItem) {
            if (item instanceof SwordItem) category = "weapon";
            else if (item instanceof PickaxeItem) category = "tool_pickaxe";
            else if (item instanceof AxeItem) category = "tool_axe";
            else if (item instanceof ShovelItem) category = "tool_shovel";
            else if (item instanceof HoeItem) category = "tool_hoe";
            else category = "tool_other";
        }
        else if (item.isEdible()) category = "food";

        MinecraftEvent craftEvent = new MinecraftEvent(
                "crafting",
                "Craft:" + craftedItem.getDisplayName().getString(),
                player
        );

        craftEvent
                .addAttribute("crafting:item", craftedItem.getDisplayName().getString())
                .addAttribute("crafting:category", category)
                .addAttribute("crafting:quantity", String.valueOf(craftedItem.getCount()))
                .addAttribute("crafting:max_stack_size", String.valueOf(craftedItem.getMaxStackSize()))
                .addAttribute("crafting:is_enchantable", String.valueOf(craftedItem.isEnchantable()))
                .addAttribute("crafting:rarity", craftedItem.getRarity().toString())
                .addAttribute("crafting:inventory_container", event.getInventory().getClass().getSimpleName());

        if (craftedItem.isDamageableItem()) {
            craftEvent.addAttribute("crafting:max_damage", String.valueOf(craftedItem.getMaxDamage()));
        }

        EventLogger.logEvent(craftEvent);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        System.out.println("Server stopping, closing log file...");
        EventLogger.closeLog();
    }

    @SubscribeEvent
    public static void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack pickedItem = event.getStack();
        ItemEntity originalEntity = event.getOriginalEntity();
        Item item = pickedItem.getItem();

        // Determine item category
        String category = "unknown";
        if (item instanceof ArmorItem) category = "armor";
        else if (item instanceof BlockItem) category = "block";
        else if (item instanceof TieredItem) {
            if (item instanceof SwordItem) category = "weapon";
            else if (item instanceof PickaxeItem) category = "tool_pickaxe";
            else if (item instanceof AxeItem) category = "tool_axe";
            else if (item instanceof ShovelItem) category = "tool_shovel";
            else if (item instanceof HoeItem) category = "tool_hoe";
            else category = "tool_other";
        }
        else if (item.isEdible()) category = "food";
        else if (item instanceof PotionItem) category = "potion";
        else if (item instanceof EnchantedBookItem) category = "enchanted_book";

        MinecraftEvent pickupEvent = new MinecraftEvent(
                "item_pickup",
                "Pickup:" + pickedItem.getDisplayName().getString(),
                player
        );

        pickupEvent
                .addAttribute("pickup:item", pickedItem.getDisplayName().getString())
                .addAttribute("pickup:category", category)
                .addAttribute("pickup:quantity", String.valueOf(pickedItem.getCount()))
                .addAttribute("pickup:location", String.format("%.1f,%.1f,%.1f",
                        player.getX(), player.getY(), player.getZ()))
                .addAttribute("pickup:dimension", player.level().dimension().location().toString())
                .addAttribute("pickup:rarity", pickedItem.getRarity().toString())
                .addAttribute("pickup:max_stack_size", String.valueOf(pickedItem.getMaxStackSize()));

        // Add enchantment information if present
        if (pickedItem.isEnchanted()) {
            pickupEvent.addAttribute("pickup:is_enchanted", "true");
            pickupEvent.addAttribute("pickup:enchantments",
                    pickedItem.getEnchantmentTags().toString());
        }

        // Add durability information for tools/armor
        if (pickedItem.isDamageableItem()) {
            pickupEvent
                    .addAttribute("pickup:durability", String.valueOf(pickedItem.getMaxDamage() - pickedItem.getDamageValue()))
                    .addAttribute("pickup:max_durability", String.valueOf(pickedItem.getMaxDamage()));
        }

        EventLogger.logEvent(pickupEvent);
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        ItemStack heldItem = event.getItemStack();

        // Handle bucket interactions
        if (heldItem.getItem() instanceof BucketItem) {
            MinecraftEvent bucketEvent = new MinecraftEvent(
                    "interaction",
                    "BucketUse:" + heldItem.getDisplayName().getString(),
                    player
            );

            bucketEvent
                    .addAttribute("interaction:item", heldItem.getDisplayName().getString())
                    .addAttribute("interaction:location", String.format("%.1f,%.1f,%.1f",
                            player.getX(), player.getY(), player.getZ()))
                    .addAttribute("interaction:dimension", player.level().dimension().location().toString())
                    .addAttribute("interaction:target_block", block.getName().getString());

            EventLogger.logEvent(bucketEvent);
        }

        // Handle container interactions
        if (block instanceof ChestBlock || block instanceof BarrelBlock ||
                block instanceof ShulkerBoxBlock || block instanceof DispenserBlock) {

            MinecraftEvent containerEvent = new MinecraftEvent(
                    "interaction",
                    "ContainerAccess:" + block.getName().getString(),
                    player
            );

            containerEvent
                    .addAttribute("interaction:container", block.getName().getString())
                    .addAttribute("interaction:location", String.format("%.1f,%.1f,%.1f",
                            player.getX(), player.getY(), player.getZ()))
                    .addAttribute("interaction:dimension", player.level().dimension().location().toString())
                    .addAttribute("interaction:held_item",
                            heldItem.isEmpty() ? "none" : heldItem.getDisplayName().getString());

            EventLogger.logEvent(containerEvent);
        }
    }

}
