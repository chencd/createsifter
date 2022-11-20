package com.oierbravo.createsifter;

import com.oierbravo.createsifter.foundation.data.recipe.ModProcessingRecipes;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class CreateSifterData implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        //gen.addProvider(new LangMerger(gen));
        //gen.addProvider(AllSoundEvents.provider(gen));

        ModProcessingRecipes.registerAllProcessingProviders(gen);
    }
}
