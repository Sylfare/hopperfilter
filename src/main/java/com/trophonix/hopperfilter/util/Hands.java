package com.trophonix.hopperfilter.util;

import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class Hands {

  public static boolean isMainHand(PlayerInteractEntityEvent event) {
    return event.getHand() == EquipmentSlot.HAND;
  }

}
