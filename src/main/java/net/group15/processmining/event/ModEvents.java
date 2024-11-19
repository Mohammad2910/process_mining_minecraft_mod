package net.group15.processmining.event;

import net.group15.processmining.TutorialMod;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TutorialMod.MOD_ID)
public class ModEvents {

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
