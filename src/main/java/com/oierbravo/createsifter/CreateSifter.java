package com.oierbravo.createsifter;

import com.oierbravo.createsifter.groups.ModGroup;
import com.oierbravo.createsifter.register.ModBlocks;
import com.oierbravo.createsifter.register.ModItems;
import com.oierbravo.createsifter.register.ModTiles;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CreateSifter implements ModInitializer {
    public static final String MODID = "createsifter";
    public static final String DISPLAY_NAME = "Create Sifter";
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final NonNullSupplier<CreateRegistrate> registrate = CreateRegistrate.lazy(MODID);

    @Override
    public void onInitialize() {
        CreateRegistrate r = registrate.get();

        new ModGroup("main");

        //ModPartials.init();

        ModBlocks.register();
        ModItems.register();
        ModTiles.register();





        //modEventBus.addGenericListener(RecipeSerializer.class, ModRecipeTypes::register);
        ModRecipeTypes.register();

        generateLangEntries();
       // DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateSifterClient.onCtorClient(modEventBus, forgeEventBus));
        r.register();
    }
    private void generateLangEntries(){

        registrate().addRawLang("createsifter.recipe.sifting", "Sifting recipe");
        registrate().addRawLang("create.recipe.sifting", "Sifting recipe");
        registrate().addRawLang("itemGroup.createsifter:main", "Create sifting");
    }
    public static CreateRegistrate registrate() {
        return registrate.get();
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MODID, path);
    }

}
