package hasjamon.block4block.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Statistic;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.concurrent.ThreadLocalRandom;

public class DirtWaterBottleListener implements Listener {

    // HIGH and ignoreCancelled: runs after claim protection (e.g. Block4Block's, at NORMAL) and respects it
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the action is right-click on block with the main hand
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // Check if the clicked block is dirt, grass, coarse dirt or rooted dirt
        Block block = event.getClickedBlock();
        if (block == null || !canTurnToMud(block)) {
            return;
        }

        // Like vanilla: adventure mode can't change blocks
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Get the item in main hand and offhand
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        // Cancel the event ONLY if the main hand is holding a shovel and the block would become a path
        if (isShovel(mainHandItem) && offHandItem.getType() == Material.GLASS_BOTTLE && canTurnToPath(block)) {
            event.setCancelled(true);
            return;
        }

        // A shovel in main hand and a water bottle (not any potion) in offhand
        if (!isShovel(mainHandItem) || !isWaterBottle(offHandItem)) {
            return;
        }

        // Like vanilla, not from below
        if (event.getBlockFace() == BlockFace.DOWN) {
            return;
        }

        // Use the offhand's water bottle instead of the shovel
        event.setCancelled(true);
        player.swingOffHand();
        emptyWaterBottle(player, EquipmentSlot.OFF_HAND);
        player.incrementStatistic(Statistic.USE_ITEM, Material.POTION);

        // Change the target block to mud, with vanilla's sounds and splash particles
        turnToMud(block);
    }

    // Vanilla's dirt-to-mud effects, then the block change
    private void turnToMud(Block block) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        for (int i = 0; i < 5; i++) {
            block.getWorld().spawnParticle(Particle.SPLASH,
                    block.getX() + random.nextDouble(), block.getY() + 1, block.getZ() + random.nextDouble(),
                    1, 0.0, 0.0, 0.0, 1.0);
        }
        block.getWorld().playSound(block.getLocation(), Sound.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        block.setType(Material.MUD);
    }

    // Like vanilla: the water bottle becomes a glass bottle, except in creative
    private void emptyWaterBottle(Player player, EquipmentSlot hand) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack bottle = inventory.getItem(hand);

        if (bottle.getAmount() > 1) {
            bottle.setAmount(bottle.getAmount() - 1);
            inventory.setItem(hand, bottle);
            for (ItemStack left : inventory.addItem(new ItemStack(Material.GLASS_BOTTLE)).values()) {
                player.getWorld().dropItem(player.getLocation(), left);
            }
        } else {
            inventory.setItem(hand, new ItemStack(Material.GLASS_BOTTLE));
        }
    }

    // Helper method to check if the item is a water bottle (a POTION whose base type is WATER)
    private boolean isWaterBottle(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta instanceof PotionMeta potionMeta && potionMeta.getBasePotionType() == PotionType.WATER;
    }

    // Helper method to check if the item is a shovel
    private boolean isShovel(ItemStack item) {
        if (item == null) {
            return false;
        }
        Material type = item.getType();
        return type == Material.WOODEN_SHOVEL ||
                type == Material.STONE_SHOVEL ||
                type == Material.IRON_SHOVEL ||
                type == Material.GOLDEN_SHOVEL ||
                type == Material.DIAMOND_SHOVEL ||
                type == Material.NETHERITE_SHOVEL;
    }

    // Helper method to check if the block can be turned into mud: vanilla's (dirt, coarse dirt, rooted dirt) and grass
    private boolean canTurnToMud(Block block) {
        return Tag.CONVERTABLE_TO_MUD.isTagged(block.getType()) || block.getType() == Material.GRASS_BLOCK;
    }

    // Helper method to check if the block can be converted to a path
    private boolean canTurnToPath(Block block) {
        Material type = block.getType();
        return type == Material.GRASS_BLOCK || type == Material.DIRT || type == Material.COARSE_DIRT;
    }
}
