package com.biscrat.minecraft.glassback;

import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

// ガラスを壊した時にドロップするプラグイン
public class GlassBack extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@SuppressWarnings("unused")
	@EventHandler
	public void onBlockBreak(BlockBreakEvent ev) {
		Player player = ev.getPlayer();
		if (player.getGameMode() == GameMode.CREATIVE) return;
		if (player.getItemInHand().containsEnchantment(new EnchantmentWrapper(33))) return; // silk touch
		dropGlass(ev.getBlock());
	}

	@SuppressWarnings("deprecation")
	private void dropGlass(Block block) {
		World world = block.getWorld();
		switch (block.getType()) {
			case GLASS:
			case THIN_GLASS:
			case STAINED_GLASS:
			case STAINED_GLASS_PANE:
				DyeColor color = DyeColor.getByData(block.getData());
				ItemStack i = new ItemStack(block.getType(), 1, (short)0, color.getData());
				world.dropItemNaturally(block.getLocation(), i);
				break;
		}
	}
}

