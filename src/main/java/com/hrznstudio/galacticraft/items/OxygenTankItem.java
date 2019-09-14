/*
 * Copyright (c) 2018-2019 Horizon Studio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hrznstudio.galacticraft.items;

import alexiil.mc.lib.attributes.Simulation;
import com.hrznstudio.galacticraft.accessor.GCPlayerAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * @author <a href="https://github.com/StellarHorizons">StellarHorizons</a>
 */
public class OxygenTankItem extends Item {
    public static String MAX_OXYGEN_NBT_KEY = "MaxOxygen";
    public static String OXYGEN_NBT_KEY = "Oxygen";

    private int maxOxygen;

    public OxygenTankItem(Settings settings) {
        super(settings);
        this.maxOxygen = getMaxDamage();
    }

    public static int getOxygenCount(ItemStack stack) {
        return stack.getOrCreateTag().getInt(OXYGEN_NBT_KEY);
    }

    public static int getMaxOxygen(ItemStack stack) {
        return stack.getOrCreateTag().getInt(MAX_OXYGEN_NBT_KEY);
    }

    @Override
    public void appendStacks(ItemGroup itemGroup_1, DefaultedList<ItemStack> list) {
        if (this.isIn(itemGroup_1)) {
            list.add(applyDefaultTags(new ItemStack(this), 0));
            list.add(applyDefaultTags(new ItemStack(this), maxOxygen));
        }
    }

    @Override
    public void onCraft(ItemStack tank, World world_1, PlayerEntity playerEntity_1) {
        applyDefaultTags(tank, 0);
    }

    private ItemStack applyDefaultTags(ItemStack item, int currentOxy) {
        CompoundTag tag = item.getOrCreateTag();
        tag.putInt(MAX_OXYGEN_NBT_KEY, this.maxOxygen);
        tag.putInt(OXYGEN_NBT_KEY, currentOxy);
        item.setDamage(getMaxDamage() - currentOxy);

        return item;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> lines, TooltipContext context) {
        lines.add(new TranslatableText("tooltip.galacticraft-rewoven.oxygen-remaining", getOxygenCount(stack) + "/" + this.maxOxygen));
        super.appendTooltip(stack, world, lines, context);
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (((GCPlayerAccessor) player).getGearInventory().getInvStack(6).isEmpty()) {
            ItemStack stack = player.getStackInHand(hand);
            player.setStackInHand(hand, ItemStack.EMPTY);
            ((GCPlayerAccessor) player).getGearInventory().setInvStack(6, stack, Simulation.ACTION);
            new TypedActionResult<>(ActionResult.SUCCESS, stack);
        } else if (((GCPlayerAccessor) player).getGearInventory().getInvStack(7).isEmpty()) {
            ItemStack stack = player.getStackInHand(hand);
            player.setStackInHand(hand, ItemStack.EMPTY);
            ((GCPlayerAccessor) player).getGearInventory().setInvStack(7, stack, Simulation.ACTION);
            new TypedActionResult<>(ActionResult.SUCCESS, stack);
        }
        return new TypedActionResult<>(ActionResult.PASS, player.getStackInHand(hand)); //TODO Put in GC Slot
    }

}
