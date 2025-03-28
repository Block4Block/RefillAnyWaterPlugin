package hasjamon.block4block.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class DirtWaterBottleListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the action is right-click on block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Check if the clicked block is dirt or similar (dirt, grass_block, etc.)
        Block block = event.getClickedBlock();
        if (block == null || !(block.getType() == Material.DIRT || block.getType() == Material.GRASS_BLOCK)) {
            return;
        }

        // Get the player and item in main hand and offhand
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        // Cancel event if the main hand is holding a shovel and the offhand contains a glass bottle
        if (isShovel(mainHandItem) && offHandItem.getType() == Material.GLASS_BOTTLE) {
            event.setCancelled(true);
            return;
        }

        // Check if the player is holding a shovel in main hand and a water bottle in offhand
        if (isShovel(mainHandItem) && offHandItem.getType() == Material.POTION) {
            // Check if the water bottle is in the offhand
            if (event.getHand() == EquipmentSlot.OFF_HAND) {
                return; // Already using offhand, nothing to change
            }

            // Force the event to treat the offhand as the used item
            event.setCancelled(true); // Cancel original event
            player.swingOffHand(); // Show offhand animation
            block.getWorld().playSound(block.getLocation(), "entity.generic.splash", 1.0F, 1.0F);

            // Simulate the offhand item usage (example: empty water bottle effect)
            offHandItem.setAmount(offHandItem.getAmount() - 1);
            player.getInventory().setItemInOffHand(new ItemStack(Material.GLASS_BOTTLE)); // Replace with glass bottle

            // Change the target block to mud
            block.setType(Material.MUD);
        }
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
}

