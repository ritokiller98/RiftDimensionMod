package com.riftmod.item;

import com.riftmod.RiftDimensionMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemTier;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RiftDimensionMod.MOD_ID);

    public static final RegistryObject<Item> RIFT_SWORD = ITEMS.register("rift_sword",
            () -> new RiftSwordItem(ItemTier.NETHERITE, 8, -2.4F,
                    new Item.Properties().tab(ItemGroup.TAB_COMBAT).stacksTo(1).fireResistant()));
}
