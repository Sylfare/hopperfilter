package com.trophonix.hopperfilter;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;

import java.util.*;

public class ItemFrames {

  private static final Map<Block, List<ItemFrame>> attachedItemFrames = new HashMap<>();

  static List<ItemFrame> findAttachedItemFrames(Block block) {
    if (block == null) return new ArrayList<>();
    List<ItemFrame> attached = block.getWorld().getNearbyEntities(block.getLocation(), 2, 2, 2).stream()
              .filter(f -> f instanceof ItemFrame).map(ItemFrame.class::cast)
              .filter(f -> block.equals(getHopperAttachedTo(f).orElse(null))).toList();
    attachedItemFrames.put(block, attached);
    return attached;
  }

  static List<ItemFrame> getAttachedItemFrames(Block block) {
    List<ItemFrame> attached = attachedItemFrames.get(block);
    return attached != null ? attached : findAttachedItemFrames(block);
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
