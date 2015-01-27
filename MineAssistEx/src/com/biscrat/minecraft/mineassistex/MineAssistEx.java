package com.biscrat.minecraft.mineassistex;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MineAssistEx extends JavaPlugin {
	private final UserConfig globalConfig = new UserConfig();
	private boolean usePermission = false;
	private final Set<Material> tools = new HashSet<>();
	private final Map<String, UserConfig> userConfig = new HashMap<>();
	private final Map<Material, DropData> dropData = new HashMap<>();

	@Override
	public void onEnable() {
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new BlockListener(this), this);
		reload();
	}

	private void reload() {
		tools.clear();
		userConfig.clear();
		dropData.clear();

		reloadConfig();
		final FileConfiguration conf = getConfig();

		globalConfig.enable = conf.getBoolean("global.enable", true);
		usePermission = conf.getBoolean("global.usePermission", false);

		conf.getStringList("tool.list").forEach(str -> tools.add(Material.valueOf(str)));

		final List<Material> ores = new LinkedList<>();
		conf.getStringList("ore.list").forEach(str -> ores.add(Material.valueOf(str)));
		for (final Material ore : ores) {
			final DropData data = new DropData(this);
			final String path = "ore." + ore.toString() + ".";
			data.type = ore;
			data.broadcast = conf.getBoolean(path + "broadcast", false);
			data.broadcastText = conf.getString(path + "broadcastText", "<NAME> broke ore");
			data.message = conf.getBoolean(path + "message", true);
			data.messageText = conf.getString(path + "messageText", "<NUM> ore broken");
			data.silkTouch = conf.getBoolean(path + "silkTouch", true);
			data.fortune = conf.getBoolean(path + "fortune", true);
			data.fortuneMultiply = conf.getBoolean(path + "fortuneMultiply", true);
			data.fortuneMax = conf.getInt(path + "fortuneMax", -1);
			conf.getStringList(path + "sameTypes").forEach(str -> data.sameTypes.add(Material.valueOf(str)));
			dropData.put(ore, data);
		}
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		return cmd.getName().equalsIgnoreCase("MineAssist")
						&& args.length != 0
						&& (doToggleCommand(sender, args[0]) || doReloadCommand(sender, args[0]));
	}
	
	enum Permission {RELOAD, TOGGLE, MINE}

	private boolean checkPermission(final CommandSender sender, final Permission permission, final boolean sendMessage) {
		if (sender.hasPermission("MineAssistEx." + permission.toString())) return true;
		if (sendMessage) sendMessage(sender, "You don't have the permission for this command.");
		return false;
	}

	private boolean doToggleCommand(final CommandSender sender, final String arg) {
		if (!arg.equalsIgnoreCase("on") && !arg.equalsIgnoreCase("off") && !arg.equalsIgnoreCase("toggle")) return false;
		if (!checkPermission(sender, Permission.TOGGLE, true)) return true;

		UserConfig conf;
		if (sender instanceof Player) {
			conf = userConfig.get(sender.getName());
			if (conf == null) {
				conf = new UserConfig();
				userConfig.put(sender.getName(), conf);
			}
		} else {
			conf = globalConfig;
		}

		if (arg.equalsIgnoreCase("on")) conf.enable = true;
		else if (arg.equalsIgnoreCase("off")) conf.enable = false;
		else if (arg.equalsIgnoreCase("toggle")) conf.enable = !conf.enable;

		sendMessage(sender, conf.enable ? "MineAssist has been activated" : "MineAssist has been deactivated");
		return true;
	}

	private boolean doReloadCommand(final CommandSender sender, final String arg) {
		if (!arg.equalsIgnoreCase("reload")) return false;
		if (!checkPermission(sender, Permission.RELOAD, true)) return true;
		reload();
		sendMessage(sender, "Reloaded");
		return true;
	}

	public boolean isEnabled(final Player player) {
		if (!globalConfig.enable) return false;
		if (usePermission && !checkPermission(player, Permission.MINE, false)) return false;
		UserConfig conf = userConfig.get(player.getName());
		if (conf == null) {
			conf = new UserConfig();
			userConfig.put(player.getName(), conf);
		}
		return conf.enable;
	}

	public DropData getDropData(final Material material) {
		return dropData.get(material);
	}

	public boolean checkTool(final Material tool) {
		return tools.contains(tool);
	}

	private void sendMessage(final CommandSender sender, final String message) {
		sender.sendMessage(ChatColor.GRAY + "[MineAssistEx]" + message);
	}

	public void broadcast(final String message){
		getServer().broadcastMessage(message);
	}

	public void message(final Player player, final String message) {
		player.sendMessage(message);
	}
}