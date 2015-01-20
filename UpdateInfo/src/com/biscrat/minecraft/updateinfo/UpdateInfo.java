package com.biscrat.minecraft.updateinfo;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateInfo extends JavaPlugin implements Listener {
	private List<List<String>> updateInfo;
	private Map<String, Integer> currentIndex;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		reload();
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent ev) {
		Player player = ev.getPlayer();
		sendMessages(player, 0);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return doUpdateInfoCommand(sender, cmd, args) || doPrevNextCommand(sender, cmd);
	}

	private void reload() {
		reloadConfig();
		FileConfiguration conf = getConfig();
		updateInfo = getUpdateInfoConfig(conf);
		currentIndex = new HashMap<>();
		saveConfig();
	}

	@SuppressWarnings("unchecked")
	private List<List<String>> getUpdateInfoConfig(FileConfiguration conf) {
		return (List<List<String>>) conf.getList("updateInfo");
	}

	private boolean doUpdateInfoCommand(CommandSender sender, Command cmd, String[] args) {
		if (cmd.getName().equalsIgnoreCase("UpdateInfo")) {
			if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
				reload();
				sender.sendMessage(ChatColor.GRAY + "UpdateInfo has been reloaded");
			} else {
				sender.sendMessage(ChatColor.GRAY + "use /updateinfo reload");
			}
			return true;
		}
		return false;
	}

	private boolean doPrevNextCommand(CommandSender sender, Command cmd) {
		if (!(sender instanceof Player)) return false;

		Player targetPlayer = ((Player) sender);
		Integer index = currentIndex.get(targetPlayer.getName());
		if (index == null) index = 0;
		if (cmd.getName().equalsIgnoreCase("prev")) {
			if (--index < 0) {
				targetPlayer.sendMessage(ChatColor.GRAY + "これ以上更新情報はありません。");
			} else {
				sendMessages(targetPlayer, index);
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("next")) {
			if (++index >= updateInfo.size()) {
				targetPlayer.sendMessage(ChatColor.GRAY + "これ以上更新情報はありません。");
			} else {
				sendMessages(targetPlayer, index);
			}
			return true;
		}
		return false;
	}

	private void sendMessages(Player targetPlayer, Integer index) {
		List<String> info = updateInfo.get(index);
		info.stream().forEach(s -> targetPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', s)));
		targetPlayer.sendMessage(ChatColor.GRAY + "/nextコマンドで過去の更新情報を見れます。/prevで戻ります。");
		currentIndex.put(targetPlayer.getName(), index);
	}
}
