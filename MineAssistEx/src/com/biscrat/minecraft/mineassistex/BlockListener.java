package com.biscrat.minecraft.mineassistex;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.BiFunction;

public class BlockListener implements Listener {
	private final MineAssistEx owner;

	public BlockListener(final MineAssistEx owner) {
		this.owner = owner;
	}

	@SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(final BlockBreakEvent ev) {
		final DropData dropData = owner.getDropData(ev.getBlock().getType());
		if (dropData == null) return;

		final Player player = ev.getPlayer();
		if (!owner.isEnabled(player) || !owner.checkTool(player.getItemInHand().getType()) || player.getGameMode() == GameMode.CREATIVE)
			return;

		final Block orgBlock = ev.getBlock();
		final ItemStack itemInHand = player.getItemInHand();
		if (orgBlock.getDrops(itemInHand).size() == 0) return;

		final Set<Location> locations = new HashSet<>();
		locations.add(orgBlock.getLocation());
		checkFaces(orgBlock, locations, dropData);

		final BiFunction<ItemStack, ItemStack, ItemStack> dropItem;
		if (dropData.silkTouch && itemInHand.containsEnchantment(Enchantment.SILK_TOUCH))
			dropItem = dropData::dropItemWithSilkTouch;
		else if (dropData.fortune && itemInHand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS))
			dropItem = dropData::dropItemWithFortune;
		else dropItem = (s, s2) -> s;

		final World world = player.getWorld();
		final Integer exp = ev.getExpToDrop();
		for (final Location location : locations) {
			final Block block = world.getBlockAt(location);
			final Collection<ItemStack> dropItems = block.getDrops(itemInHand);
			Iterator<ItemStack> it = dropItems.iterator();
			final ItemStack item = it.next();
			it.forEachRemaining(i -> item.setAmount(item.getAmount() + i.getAmount()));
			world.dropItemNaturally(location, dropItem.apply(item, itemInHand));
			(world.spawn(location, ExperienceOrb.class)).setExperience(exp);
			block.setType(Material.AIR);
		}
	}

	private final BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

	private void checkFaces(final Block block, final Set<Location> locations, final DropData dropData) {
		for (final BlockFace face : faces) {
			final Block b = block.getRelative(face);
			if (dropData.compare(b.getType()) && !locations.contains(b.getLocation())) {
				locations.add(b.getLocation());
				final Integer maxBlock = 100;
				if (locations.size() > maxBlock) return;
				checkFaces(b, locations, dropData);
			}
		}
	}
}
