/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.item.v1;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.dispenser.ShearsDispenserBehavior;
import net.minecraft.enchantment.EfficiencyEnchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;

import net.fabricmc.fabric.impl.tag.convention.TagRegistration;

/**
 * General-purpose Fabric-provided extensions for {@link Item} subclasses.
 *
 * <p>Note: This interface is automatically implemented on all items via Mixin and interface injection.
 *
 * <p>Note to maintainers: Functions should only be added to this interface if they are general-purpose enough,
 * to be evaluated on a case-by-case basis. Otherwise, they are better suited for more specialized APIs.
 */
public interface FabricItem {
	TagKey<Item> FABRIC_SHEARS = TagRegistration.ITEM_TAG_REGISTRATION.registerFabric("shears");

	/**
	 * When the NBT of an item stack in the main hand or off hand changes, vanilla runs an "update animation".
	 * This function is called on the client side when the NBT or count of the stack has changed, but not the item,
	 * and returning false cancels this animation.
	 *
	 * @param player   the current player; this may be safely cast to {@link net.minecraft.client.network.ClientPlayerEntity} in client-only code
	 * @param hand     the hand; this function applies both to the main hand and the off hand
	 * @param oldStack the previous stack, of this item
	 * @param newStack the new stack, also of this item
	 * @return true to run the vanilla animation, false to cancel it.
	 */
	@SuppressWarnings("JavadocReference")
	default boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
		return true;
	}

	/**
	 * When the NBT of the selected stack changes, block breaking progress is reset.
	 * This function is called when the NBT of the selected stack has changed,
	 * and returning true allows the block breaking progress to continue.
	 *
	 * @param player   the player breaking the block
	 * @param oldStack the previous stack, of this item
	 * @param newStack the new stack, also of this item
	 * @return true to allow continuing block breaking, false to reset the progress.
	 */
	default boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
		return false;
	}

	/**
	 * Return the attribute modifiers to apply when this stack is worn in a living entity equipment slot.
	 * Stack-aware version of {@link Item#getAttributeModifiers(EquipmentSlot)}.
	 *
	 * <p>Note that attribute modifiers are only updated when the stack changes, i.e. when {@code ItemStack.areEqual(old, new)} is false.
	 *
	 * @param stack the current stack
	 * @param slot  the equipment slot this stack is in
	 * @return the attribute modifiers
	 */
	default Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
		return ((Item) this).getAttributeModifiers(slot);
	}

	/**
	 * Determines if mining with this item allows drops to be harvested from the specified block state.
	 * Stack-aware version of {@link Item#isSuitableFor(BlockState)}.
	 *
	 * @param stack the current stack
	 * @param state the block state of the targeted block
	 * @return true if drops can be harvested
	 */
	default boolean isSuitableFor(ItemStack stack, BlockState state) {
		return ((Item) this).isSuitableFor(state);
	}

	/**
	 * Determines if this item should behave like shears.
	 * To act like shears means to be able to {@linkplain Shearable#sheared(SoundCategory) shear mobs}, pumpkins, and beehives, {@linkplain MatchToolLootCondition harvest grass, cobwebs, and etc.}, be {@linkplain EfficiencyEnchantment#isAcceptableItem(ItemStack) enchanted with efficiency}, disarm tripwire, and have {@link ShearsDispenserBehavior} if there isn't one already registered.
	 *
	 * <p>The default implementation checks if {@code this} is an instance of {@link ShearsItem} or in the {@link #FABRIC_SHEARS #fabric:shears} tag.
	 *
	 * <p>If you want to check if a stack should be shears, it is recommended to use the stack version of this method: {@link FabricItemStack#isShears()}.
	 *
	 * @param stack the current stack
	 * @return {@code true} if this item should behave like shears.
	 */
	default boolean isShears(ItemStack stack) {
		return this instanceof ShearsItem || stack.isIn(FABRIC_SHEARS);
	}

	/**
	 * Returns a leftover item stack after {@code stack} is consumed in a recipe.
	 * (This is also known as "recipe remainder".)
	 * For example, using a lava bucket in a furnace as fuel will leave an empty bucket.
	 *
	 * <p>Here is an example for a recipe remainder that increments the item's damage.
	 *
	 * <pre>{@code
	 *  if (stack.getDamage() < stack.getMaxDamage() - 1) {
	 *  	ItemStack moreDamaged = stack.copy();
	 *  	moreDamaged.setDamage(stack.getDamage() + 1);
	 *  	return moreDamaged;
	 *  }
	 *
	 *  return ItemStack.EMPTY;
	 * }</pre>
	 *
	 *
	 * <p>This is a stack-aware version of {@link Item#getRecipeRemainder()}.
	 *
	 * <p>Note that simple item remainders can also be set via {@link Item.Settings#recipeRemainder(Item)}.
	 *
	 * <p>If you want to get a remainder for a stack,
	 * is recommended to use the stack version of this method: {@link FabricItemStack#getRecipeRemainder()}.
	 *
	 * @param stack the consumed {@link ItemStack}
	 * @return the leftover item stack
	 */
	@SuppressWarnings("DataFlowIssue")
	default ItemStack getRecipeRemainder(ItemStack stack) {
		return ((Item) this).hasRecipeRemainder() ? ((Item) this).getRecipeRemainder().getDefaultStack() : ItemStack.EMPTY;
	}

	/**
	 * This is a stack-aware version of {@link Item#getFoodComponent()}.
	 * Note that simple food component can also be set via {@link Item.Settings#food(FoodComponent)}.
	 * If you want to get a food component for a stack, is <strong>recommended</strong> to use the stack version of this method: {@link FabricItemStack#getFoodComponent()}.
	 *
	 * @return this item's {@link FoodComponent}, or {@code null} if none was set
	 */
	default @Nullable FoodComponent getFoodComponent(ItemStack stack) {
		return ((Item) this).getFoodComponent();
	}
}
