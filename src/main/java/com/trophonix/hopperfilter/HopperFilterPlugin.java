package com.trophonix.hopperfilter;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.Hopper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.trophonix.hopperfilter.util.Items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HopperFilterPlugin extends JavaPlugin implements Listener {

  private ConfigMessage filterFlippedUp;
  private ConfigMessage filterFlippedDown;

  @Override public void onEnable() {
    getConfig().options().copyDefaults(true);
    saveConfig();
    load();
    getServer().getPluginManager().registerEvents(this, this);
  }

  private void load() {
    this.filterFlippedUp = new ConfigMessage(getConfig(), "filterFlippedUp");
    this.filterFlippedDown = new ConfigMessage(getConfig(), "filterFlippedDown");
  }

  @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (args.length > 0 && (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload"))) {
      reloadConfig();
      load();
      sender.sendMessage(Component.text("Reloaded config.", NamedTextColor.GREEN));
      return true;
    }
    return false;
  }

  @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    return super.onTabComplete(sender, command, alias, args);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onItemFrameSpawn(HangingPlaceEvent event) {
    if (event.getEntity() instanceof ItemFrame) {
      Bukkit.getScheduler().runTaskLater(this, () ->
          ItemFrames.getHopperAttachedTo((ItemFrame)event.getEntity()).ifPresent(b ->
            ItemFrames.addAttachedItemFrame(b, (ItemFrame)event.getEntity())), 1L);
    }
  }

  @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onItemFrameBreak(HangingBreakEvent event) {
    if (event.getEntity() instanceof ItemFrame) {
      ItemFrames.removeItemFrame((ItemFrame)event.getEntity());
    }
  }

  @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onFrameRightClick(PlayerInteractEntityEvent event) {
    if (event.getRightClicked() instanceof ItemFrame) {
      ItemFrame frame = (ItemFrame) event.getRightClicked();
      if (event.getHand() != EquipmentSlot.HAND) return;
      if (!frame.getItem().getType().equals(Material.AIR)) ItemFrames.getHopperAttachedTo(frame).ifPresent(
          block -> {
            event.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(this, () -> {
              Rotation newRot = frame.getRotation() == Rotation.NONE ?
                                    Rotation.FLIPPED : Rotation.NONE;
              frame.setRotation(newRot);
              (newRot == Rotation.NONE ? filterFlippedUp : filterFlippedDown)
                  .send(event.getPlayer());
            }, 1L);
      });
    }
  }

  @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onHopperPickup(InventoryPickupItemEvent event) {
    if (!(event.getInventory().getHolder() instanceof Hopper)) return;
    Hopper hopper = (Hopper)event.getInventory().getHolder();
    event.setCancelled(cancel(hopper, event.getItem().getItemStack()));
  }

  @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onHopperMove(InventoryMoveItemEvent event) {
    if (!(event.getDestination().getHolder() instanceof Hopper)) return;
    Hopper hopper = (Hopper)event.getDestination().getHolder();
    event.setCancelled(cancel(hopper, event.getItem()));
  }

  private boolean cancel(Hopper hopper, ItemStack item) {
    if (hopper == null) return false;
    List<ItemFrame> frames = ItemFrames.getAttachedItemFrames(hopper.getBlock());
    if (frames == null || frames.isEmpty()) return false;
    boolean inclusive = false;
    for (ItemFrame frame : frames) {
      ItemStack fItem = frame.getItem();
      if (fItem.getType().equals(Material.AIR)) continue;
      if (frame.getRotation() == Rotation.NONE) {
        inclusive = true;
        if (Items.matchBroadly(fItem, item)) {
          return false;
        }
      } else if (frame.getRotation() == Rotation.FLIPPED) {
        if (Items.matchBroadly(fItem, item)) {
          return true;
        }
      }
    }
    return inclusive;
  }

}
