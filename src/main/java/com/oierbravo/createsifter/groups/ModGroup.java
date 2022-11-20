package com.oierbravo.createsifter.groups;


import com.oierbravo.createsifter.CreateSifter;
import com.oierbravo.createsifter.register.ModBlocks;
import io.github.fabricators_of_create.porting_lib.util.LazyItemGroup;
import net.minecraft.world.item.ItemStack;


public class ModGroup extends LazyItemGroup {
	public static ModGroup MAIN;;
	
	public ModGroup(String name) {
		super(CreateSifter.MODID+":"+name);
		MAIN = this;
	}

	@Override
	public ItemStack makeIcon() {
		return new ItemStack(ModBlocks.SIFTER.get());
	}
}
