package org.huajiager.client.render.entity;

import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.util.NBTHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

/**
 * 誓约旗 / 无限耐久护身符的头戴图层。
 *
 * <p>判定：手持带 {@code orga} 标记的护身符，或背包里有护身符（穿戴整套奥尔加时换成誓约旗），
 * 或手持护身符时，把对应物品模型按 HEAD 变换画在玩家头部。</p>
 */
public class CharmDisplayFeatureRenderer
        extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    public CharmDisplayFeatureRenderer(
            FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            AbstractClientPlayerEntity player, float limbAngle, float limbDistance, float tickDelta,
            float animationProgress, float headYaw, float headPitch) {
        ItemStack charm = new ItemStack(ItemLoader.infiniteCharm);
        ItemStack flag = new ItemStack(ItemLoader.orgaFlag);
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();

        boolean holdsCharm = main.isOf(ItemLoader.infiniteCharm) || off.isOf(ItemLoader.infiniteCharm);
        boolean holdsOrgaCharm = (main.isOf(ItemLoader.infiniteCharm) && hasOrgaTag(main))
                || (off.isOf(ItemLoader.infiniteCharm) && hasOrgaTag(off));
        boolean charmInInventory = false;
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).isOf(ItemLoader.infiniteCharm)) {
                charmInInventory = true;
                break;
            }
        }

        ItemStack shown;
        if (holdsOrgaCharm) {
            shown = flag;
        } else if (charmInInventory && !holdsCharm) {
            shown = NBTHelper.getEntityBoolean(player, "huajiage.orga.suit") ? flag : charm;
        } else if (holdsCharm) {
            shown = charm;
        } else {
            return;
        }

        matrices.push();
        // 挂在头部：跟玩家模型 head 部件的位姿走（物品用 HEAD 展示变换，原版观感约一格大小）
        this.getContextModel().head.rotate(matrices);
        matrices.translate(0.0, -0.25, 0.0);
        MinecraftClient.getInstance().getItemRenderer().renderItem(player, shown,
                ModelTransformationMode.HEAD, false, matrices, vertexConsumers, player.getWorld(), light,
                OverlayTexture.DEFAULT_UV, player.getId());
        matrices.pop();
    }

    private static boolean hasOrgaTag(ItemStack stack) {
        return NBTHelper.getTagCompoundSafe(stack).getBoolean("orga");
    }
}
