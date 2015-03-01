package com.biscrat.minecraft.tpPlayers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class TpPlayers extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		worlds = new HashMap<>();
		recorededPlayers = new HashSet<>();
		loadPlayersWorld();

		saveDefaultConfig();
		reload();

		getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		savePlayersWorld();
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	@SuppressWarnings("unused")
	public void onJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		// if the player was in 'fromWorlds', teleport him/her to 'toWorld'
		String world = getPlayerWorldName(player);
		if (fromWorlds.contains(world)) {
			final Location toLocation = getServer().getWorld(toWorld).getSpawnLocation();
			player.teleport(toLocation);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	@SuppressWarnings("unused")
	public void onWorldChanged(PlayerChangedWorldEvent event) {
		final Player player = event.getPlayer();
		final World world = player.getWorld();
		recordPlayerWorld(player, world);
	}


	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	@SuppressWarnings("unused")
	public void onLeave(PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final World world = player.getWorld();
		recordPlayerWorld(player, world);
		savePlayersWorld();
	}


	private String toWorld;
	private List<String> fromWorlds;
	private Map<String, String> worlds;
	private Set<String> recorededPlayers;

	private void reload() {
		reloadConfig();
		final FileConfiguration conf = getConfig();
		toWorld = conf.getString("info.toWorld");
		fromWorlds = conf.getStringList("info.fromWorlds");
	}

	private void recordPlayerWorld(final Player player, final World world) {
		final String playerName = player.getName();
		worlds.put(playerName, world.getName());
		recorededPlayers.add(playerName);
	}

	private String getPlayerWorldName(final Player player) {
		String recordedWorldName = worlds.get(player.getName());
		if (recordedWorldName != null) {
			return recordedWorldName;
		} else {
			return player.getWorld().getName();
		}
	}

	private void savePlayersWorld() {
		final FileConfiguration conf = new YamlConfiguration();
		conf.set("worlds.list", recorededPlayers.toArray());
		conf.set("worlds.player", worlds);
		final File file = new File(getDataFolder(), "worlds.yml");
		try {
			conf.save(file);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "TpPlayers failed to save worlds.yml. " + e.getMessage());
		}
	}

	private void loadPlayersWorld() {
		final File file = new File(getDataFolder(), "worlds.yml");
		final FileConfiguration conf = YamlConfiguration.loadConfiguration(file);

		final List<String> players = conf.getStringList("worlds.list");
		for(String player : players) {
			if (player.length() == 0) continue;
			final String world = conf.getString("worlds.player." + player);
			if (world.length() == 0) continue;
			worlds.putIfAbsent(player, world);
			recorededPlayers.add(player);
		}
	}
}