package com.trophonix.hopperfilter.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Items {

  public static boolean matchBroadly(ItemStack base, ItemStack other) {
    other = other.clone();
    if (base.hasItemMeta()) {
      ItemMeta bM = base.getItemMeta();
      ItemMeta oM = other.getItemMeta();
      assert bM != null;
      assert oM != null;
      if (!bM.hasDisplayName()) {
        oM.displayName(null);
      }
      if (!bM.hasLore()) {
        oM.lore(null);
      }
    }
    return base.isSimilar(other);
  }

}
