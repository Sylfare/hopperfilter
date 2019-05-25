package com.trophonix.hopperfilter;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ConfigMessage {

  private String[] message;

  public ConfigMessage(ConfigurationSection config, String location, String... defaultMessage) {
    if (config.isString(location)) {
      this.message = new String[]{colorize(Objects.requireNonNull(config.getString(location)))};
    } else if (config.isList(location)) {
      List<String> message = config.getStringList(location);
      for (int i = 0; i < message.size(); i++) {
        message.set(i, colorize(message.get(i)));
      }
      this.message = message.toArray(new String[0]);
    } else this.message = defaultMessage;
    for (int i = 0; i < this.message.length; i++) {
      String line = this.message[i];
      if (line == null || line.isEmpty()) continue;
      this.message[i] = colorize(line);
    }
  }

  public void send(CommandSender receiver, String... placeholders) {
    receiver.sendMessage(Arrays.stream(this.message).map(str -> {
      for (int i = 0; i < placeholders.length - 1; i += 2) {
        str = str.replace(placeholders[i], placeholders[i + 1]);
      }
      return str;
    }).toArray(String[]::new));
  }

  public void send(Collection<? extends CommandSender> receivers, String... placeholders) {
    receivers.forEach(receiver -> send(receiver, placeholders));
  }

  private static String colorize(String string) {
    return ChatColor.translateAlternateColorCodes('&', string);
  }

}
