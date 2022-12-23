package com.oierbravo.createsifter.content.contraptions.components.sifter;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.oierbravo.createsifter.register.ModPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.millstone.MillstoneRenderer;
import com.simibubi.create.content.contraptions.processing.BasinBlock;
import com.simibubi.create.foundation.render.CachedBufferer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SifterRenderer extends MillstoneRenderer {
    public SifterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {


        //boolean usingFlywheel = Backend.canUseInstancing(te.getLevel());
        SifterTileEntity sifterTileEntity = (SifterTileEntity) te;
        VertexConsumer vb = buffer.getBuffer(RenderType.cutout());
        ItemStack meshItemStack = sifterTileEntity.meshInv.getStackInSlot(0);
        if(!meshItemStack.isEmpty()){
            BlockState state = getRenderedBlockState(te);
            PartialModel meshModel = ModPartials.getFromItemStack(meshItemStack);

            CachedBufferer.partial(meshModel,state)

                    .translateY(1.01)
                    .light(light)
                    .renderInto(ms, vb);
        }
        //In progress Block renderer
        /*ItemStack inProccessItemStack = sifterTileEntity.getInputItemStack();

        if(!inProccessItemStack.equals(ItemStack.EMPTY)){
            float progress = sifterTileEntity.getProcessingRemainingPercent();
            ms.pushPose();
            TransformStack.cast(ms)
                    .scale((float) .9)
                    .translate(new Vec3(0.55, 1+ 1* progress, 0.55));
            renderBlock(ms,buffer,light, overlay,sifterTileEntity.getInputItemStack());
            ms.popPose();
        }*/
        super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
    }
    protected void renderBlock(PoseStack ms, MultiBufferSource buffer, int light, int overlay, ItemStack stack) {
        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(stack, ItemTransforms.TransformType.NONE, light, overlay, ms, buffer, 0);
    }
}
