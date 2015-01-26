package com.biscrat.minecraft.mineassistex;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.IntBinaryOperator;

public class DropData {
	public Material type;
	public Boolean silkTouch;
	public Boolean fortune;
	public Boolean fortuneMultiply;
	public Integer fortuneMax;
	public List<Material> sameTypes = new LinkedList<>();
	public Random rand = new Random();
	
	public boolean compare(final Material type) {
		return sameTypes.contains(type);
	}

	@SuppressWarnings("unused")
	public ItemStack dropItemWithSilkTouch(final ItemStack normalDropItem, final ItemStack itemInHand) {
		return new ItemStack(sameTypes.get(0));
	}

	public ItemStack dropItemWithFortune(final ItemStack normalDropItem, final ItemStack itemInHand) {
		final Integer r = rand.nextInt(1000);
		Integer amount = normalDropItem.getAmount();
		IntBinaryOperator calculate = (lhs, rhs) -> lhs * rhs;
		if (!fortuneMultiply) calculate = (lhs, rhs) -> lhs + rhs;

		switch (itemInHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)) {
			case 1:
				if (r < 333) amount = calculate.applyAsInt(amount, 2);
				break;
			case 2:
				if (r < 250) amount = calculate.applyAsInt(amount, 3);
				else if (r < 500) amount = calculate.applyAsInt(amount, 2);
				break;
			case 3:
				if (r < 200) amount = calculate.applyAsInt(amount, 4);
				else if (r < 400) amount = calculate.applyAsInt(amount, 3);
				else if (r < 600) amount = calculate.applyAsInt(amount, 2);
				break;
		}
		if (fortuneMax >= 0 && amount > fortuneMax) amount = fortuneMax;
		normalDropItem.setAmount(amount);
		return normalDropItem;
	}
}
