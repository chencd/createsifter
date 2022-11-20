package com.oierbravo.createsifter;

import com.oierbravo.createsifter.content.contraptions.components.meshes.MeshItemRenderer;
import com.oierbravo.createsifter.register.ModPartials;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

public class CreateSifterClient implements ClientModInitializer {
    public void onInitializeClient() {
        ModPartials.load();
    }

    public static void registerItemRenderer(@NotNull ItemLike item) {
        BuiltinItemRendererRegistry.INSTANCE.register(item, new MeshItemRenderer());
    }
}
