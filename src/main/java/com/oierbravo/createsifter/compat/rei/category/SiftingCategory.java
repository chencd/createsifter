package com.oierbravo.createsifter.compat.rei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import com.oierbravo.createsifter.compat.jei.category.animations.AnimatedSifter;
import com.oierbravo.createsifter.content.contraptions.components.sifter.SiftingRecipe;
import com.oierbravo.createsifter.foundation.gui.ModGUITextures;
import com.simibubi.create.compat.rei.category.CreateRecipeCategory;
import com.simibubi.create.compat.rei.category.WidgetUtil;
import com.simibubi.create.compat.rei.display.CreateDisplay;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.util.ClientEntryStacks;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;

import java.util.Iterator;
import java.util.List;

public class SiftingCategory extends CreateRecipeCategory<SiftingRecipe> {
    private AnimatedSifter sifter = new AnimatedSifter();

    public SiftingCategory(CreateRecipeCategory.Info<SiftingRecipe> info) {
        super(info);
    }


    public static AllGuiTextures getRenderedSlot(float chance) {
        if (chance == 1)
            return AllGuiTextures.JEI_SLOT;

        return AllGuiTextures.JEI_CHANCE_SLOT;
    }

    @Override
    public void addWidgets(CreateDisplay<SiftingRecipe> display, List<Widget> ingredients, Point origin) {
        SiftingRecipe recipe = display.getRecipe();
        ingredients.add(WidgetUtil.textured(AllGuiTextures.JEI_SLOT, origin.getX() + 15, origin.getY() + 9));
        ingredients.add(basicSlot(16, 10, origin).markInput().entries(EntryIngredients.ofIngredient(recipe.getSiftableIngredient())));

        if(!recipe.getMeshIngredient().isEmpty()) {
            ingredients.add(WidgetUtil.textured(AllGuiTextures.JEI_SLOT, origin.getX() + 15, origin.getY() + 24));
            ingredients.add(basicSlot(16, 25, origin).entries(EntryIngredients.ofIngredient(recipe.getMeshIngredient())));
        }

        List<ProcessingOutput> results = recipe.getRollableResults();
        boolean single = results.size() == 1;
        int i = 0;

        for(Iterator var7 = results.iterator(); var7.hasNext(); ++i) {
            ProcessingOutput output = (ProcessingOutput)var7.next();
            int xOffset = i % 4 * 19;
            int yOffset = i / 4 * 19;
            ingredients.add(WidgetUtil.textured(getRenderedSlot(output.getChance()), origin.getX() + (single ? 139 : 100 + xOffset), origin.getY() + 2 + yOffset));
            Slot outputSlot = basicSlot(single ? 140 : 101 + xOffset, 3 + yOffset, origin).markOutput()
                    .entry(EntryStacks.of(output.getStack()));
            ClientEntryStacks.setTooltipProcessor(outputSlot.getCurrentEntry(), (entryStack, tooltip) -> {
                addStochasticTooltip(output, tooltip);
                return tooltip;
            });
            ingredients.add(outputSlot);
        }
    }

    public void draw(SiftingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {


        List<ProcessingOutput> results = recipe.getRollableResults();
        boolean single = results.size() == 1;
        if(single){
            AllGuiTextures.JEI_ARROW.render(matrixStack, 85, 32); //Output arrow
        } else {
            ModGUITextures.JEI_SHORT_ARROW.render(matrixStack, 75, 32); //Output arrow
        }

        AllGuiTextures.JEI_DOWN_ARROW.render(matrixStack, 43, 4);
        sifter.draw(matrixStack, 48, 27);
    }
}