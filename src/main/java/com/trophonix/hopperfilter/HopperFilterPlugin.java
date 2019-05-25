package com.trophonix.hopperfilter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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
      sender.sendMessage(ChatColor.GREEN + "Reloaded config.");
      return true;
    }
    return false;
  }

  @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    return super.onTabComplete(sender, command, alias, args);
  }

  @EventHandler
  public void onItemFrameSpawn(EntitySpawnEvent event) {
    if (event.getEntity() instanceof ItemFrame) {
      Bukkit.getScheduler().runTaskLater(this, () ->
          ItemFrames.getHopperAttachedTo((ItemFrame)event.getEntity()).ifPresent(b ->
            ItemFrames.addAttachedItemFrame(b, (ItemFrame)event.getEntity())), 1L);
    }
  }

  @EventHandler
  public void onItemFrameBreak(HangingBreakEvent event) {
    if (event.getEntity() instanceof ItemFrame) {
      ItemFrames.removeAttachedItemFrame(event.getEntity().getLocation().getBlock(), (ItemFrame)event.getEntity());
    }
  }

  @EventHandler
  public void onFrameRightClick(PlayerInteractAtEntityEvent event) {
    if (event.getRightClicked() instanceof ItemFrame) {
      ItemFrame frame = (ItemFrame) event.getRightClicked();
      ItemFrames.getHopperAttachedTo(frame).ifPresent(
          block -> Bukkit.getScheduler().runTaskLater(this,
            () -> {
              Rotation newRot = frame.getRotation().rotateCounterClockwise() == Rotation.NONE ?
                                Rotation.FLIPPED : Rotation.NONE;
              frame.setRotation(newRot);
              (newRot == Rotation.NONE ? filterFlippedUp : filterFlippedDown)
                  .send(event.getPlayer());
            }, 1L));
    }
  }

  @EventHandler
  public void onHopperPickup(InventoryPickupItemEvent event) {
    if (event.getInventory().getType() != InventoryType.HOPPER) return;
    Hopper hopper = (Hopper)event.getInventory().getHolder();
    event.setCancelled(cancel(hopper, event.getItem().getItemStack()));
  }

  @EventHandler
  public void onHopperMove(InventoryMoveItemEvent event) {
    if (event.getInitiator().getType() != InventoryType.HOPPER) return;
    Hopper hopper = (Hopper)event.getInitiator().getHolder();
    event.setCancelled(cancel(hopper, event.getItem()));
    if (event.getInitiator().equals(event.getSource())) {
      Block below = hopper.getWorld().getBlockAt(hopper.getLocation().clone().add(0, -1, 0));
      if (below.getType() == Material.HOPPER && ItemFrames.getAttachedItemFrames(below).size() > 0) {
        event.setCancelled(!cancel((Hopper)below.getState(), event.getItem()));
      }
    }
  }

  private boolean cancel(Hopper hopper, ItemStack item) {
    if (hopper == null) return false;
    boolean inverted = true;
    for (ItemFrame frame : ItemFrames.getAttachedItemFrames(hopper.getBlock())) {
      if (frame.getRotation() == Rotation.NONE) {
        inverted = false;
        if (frame.getItem().isSimilar(item)) {
          return false;
        }
      } else if (frame.getRotation() == Rotation.FLIPPED) {
        if (frame.getItem().isSimilar(item)) {
          return true;
        }
      }
    }
    return !inverted;
  }

}