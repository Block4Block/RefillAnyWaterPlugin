package hasjamon.block4block;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BlockIterator;

public class RefillAnyWaterPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getLogger().info("RefillAnyWaterPlugin has been enabled!");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("RefillAnyWaterPlugin has been disabled!");
    }

    @EventHandler
    public void onBottleFill(PlayerInteractEvent event) {

        // Check if it's a right-click action
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        // Check for bottles in both hands
        if (tryFillBottle(player, event.getHand())) {
            // Cancel the event to prevent default behavior
            event.setCancelled(true);
        }
    }

    // Try to fill a bottle in the specified hand
    private boolean tryFillBottle(Player player, EquipmentSlot hand) {
        ItemStack item = player.getInventory().getItem(hand);

        // Check if the player is holding a glass bottle
        if (item == null || item.getType() != Material.GLASS_BOTTLE) {
            return false;
        }

        // Check for water within 3 blocks in front of the player
        Block waterBlock = getNearbyWaterBlock(player);
        if (waterBlock == null) {
            return false;
        }

        // Create a correctly filled WATER_BOTTLE
        ItemStack waterBottle = createWaterBottle();

        // Refill the bottle correctly
        if (item.getAmount() == 1) {
            player.getInventory().setItem(hand, waterBottle);
        } else {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().addItem(waterBottle);
        }

        // Play the water filling sound
        player.playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1.0f, 1.0f);

        return true;
    }

    // Check for a valid water block within 4.5 blocks from the player's eye level
    private Block getNearbyWaterBlock(Player player) {
        double maxDistance = 4.5; // Block interaction range in survival mode
        BlockIterator blockIterator = new BlockIterator(player.getEyeLocation(), 0.1, (int) Math.ceil(maxDistance));
        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();
            if (isWaterBlock(block)) {
                return block;
            }
        }
        return null;
    }

    // Check if the block is water or a valid levelled water block
    private boolean isWaterBlock(Block block) {
        if (block.getType() == Material.WATER) {
            if (block.getBlockData() instanceof Levelled level) {
                return level.getLevel() < level.getMaximumLevel(); // Check if it's not empty
            }
            return true; // Regular water block
        }
        return false;
    }

    // Create a valid WATER_BOTTLE that retains vanilla functionality
    private ItemStack createWaterBottle() {
        ItemStack waterBottle = new ItemStack(Material.POTION, 1);
        PotionMeta meta = (PotionMeta) waterBottle.getItemMeta();
        if (meta != null) {
            meta.setBasePotionType(PotionType.WATER); // Correct method to set base type
            waterBottle.setItemMeta(meta);
        }
        return waterBottle;
    }
}
