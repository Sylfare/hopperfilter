package com.trophonix.hopperfilter;

import java.util.Collection;
import java.util.Dictionary;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ConfigMessage {

  private MiniMessage miniMessage = MiniMessage.miniMessage();

  private Component message;

  public ConfigMessage(ConfigurationSection config, @NotNull String location, Component defaultMessage) {
    String serializedMessage;
    if(config.isList(location)){
      serializedMessage = config.getList(location).stream().map(Object::toString).collect(Collectors.joining("<newline>"));
    } else {
      serializedMessage = config.getString(location, "");
    }


    this.message = miniMessage.deserialize(serializedMessage);
    
  }

  public ConfigMessage(FileConfiguration config, String location) {
    this(config, location, Component.text(""));
  }

  public void send(CommandSender receiver) {
    this.send(receiver, null);
  }

  public void send(CommandSender receiver, Dictionary<String,String> placeholders) {
    // placeholders does not seem to be used yet
    receiver.sendMessage(message);
  }

  public void send(Collection<? extends CommandSender> receivers, Dictionary<String,String> placeholders) {
    receivers.forEach(receiver -> send(receiver, placeholders));
  }
}
