package com.trophonix.hopperfilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

public class ItemFrames {

  private static final Map<Block, List<ItemFrame>> attachedItemFrames = new HashMap<>();

  static List<ItemFrame> findAttachedItemFrames(Block block) {
    if (block == null) return new ArrayList<>();
    List<ItemFrame> attached = block.getWorld().getNearbyEntities(block.getLocation(), 2, 2, 2).stream()
              .filter(f -> f instanceof ItemFrame).map(ItemFrame.class::cast)
              .filter(f -> block.equals(getHopperAttachedTo(f).orElse(null))).collect(Collectors.toList());
    attachedItemFrames.put(block, attached);
    return attached;
  }

  static List<ItemFrame> getAttachedItemFrames(Block block) {
    List<ItemFrame> attached = attachedItemFrames.get(block);
    return attached != null ? attached : findAttachedItemFrames(block);
  }

  static boolean isFilterInverted(ItemFrame itemFrame) {
    return itemFrame.getRotation() == Rotation.FLIPPED;
  }
  static HashMap<ItemStack, Boolean> getAttachedFilters(Block block) {
    return (HashMap<ItemStack, Boolean>) getAttachedItemFrames(block).stream()
      .filter(itemFrame -> itemFrame.getItem().getType() != Material.AIR)
      .collect(Collectors.toMap(ItemFrame::getItem, ItemFrames::isFilterInverted));
  }

  static void addAttachedItemFrame(Block block, ItemFrame frame) {
    List<ItemFrame> attached = attachedItemFrames.get(block);
    if (attached == null) attached = new ArrayList<>();
    attached.add(frame);
    attachedItemFrames.put(block, attached);
  }

  static void removeAttachedItemFrame(Block block, ItemFrame frame) {
    List<ItemFrame> attached = attachedItemFrames.get(block);
    if (attached != null) {
      attached.remove(frame);
      attachedItemFrames.put(block, attached);
    }
  }

  static void removeItemFrame(ItemFrame frame) {
    getHopperAttachedTo(frame).ifPresent(block ->
        removeAttachedItemFrame(block, frame));
  }

  public static Optional<Block> getHopperAttachedTo(ItemFrame frame) {
    Block attached = frame.getLocation().getBlock().getRelative(frame.getAttachedFace());
    if (attached.getType() != Material.HOPPER) return Optional.empty();
    return Optional.of(attached);
  }

}
