package hasjamon.block4block.listener;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class PlayerInWaterListener implements Listener {

    @EventHandler
    public void onBottleFill(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check if player is standing in water
        if (!isPlayerInWater(player)) {
            return;
        }

        // Check if the action is a right-click
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            // Try to fill a bottle from the main or off hand
            if (tryFillBottle(player, EquipmentSlot.HAND) || tryFillBottle(player, EquipmentSlot.OFF_HAND)) {
                // Cancel event to prevent default behavior
                event.setCancelled(true);
            }
        }
    }

    // Check if the player is standing in water
    private boolean isPlayerInWater(Player player) {
        Block block = player.getLocation().getBlock();
        return block.isLiquid() || block.getType() == Material.WATER;
    }

    // Try to fill a bottle in the specified hand
    private boolean tryFillBottle(Player player, EquipmentSlot hand) {
        ItemStack item = player.getInventory().getItem(hand);

        // Check if the player is holding a glass bottle
        if (item == null || item.getType() != Material.GLASS_BOTTLE) {
            return false;
        }

        // Create a correctly filled water bottle
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

    // Create a valid WATER_BOTTLE that retains vanilla functionality
    private ItemStack createWaterBottle() {
        ItemStack waterBottle = new ItemStack(Material.POTION, 1);
        PotionMeta meta = (PotionMeta) waterBottle.getItemMeta();
        if (meta != null) {
            meta.setBasePotionType(PotionType.WATER);
            waterBottle.setItemMeta(meta);
        }
        return waterBottle;
    }
}
