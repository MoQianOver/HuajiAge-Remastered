package org.huajiager.mixin.client.tlm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair;

/**
 * 车万女仆（TLMO）EntityChair.getYaw() 自递归崩溃的兼容修复。
 *
 * <p>TLMO 0.8.2 里 getYaw() 的实现是 {@code return getYaw();}（自己调自己），
 * 只要渲染到"椅子"物品（例如创造模式物品栏的标签页图标）就会无限递归到 StackOverflow 崩溃。
 * 这里拦下那次自递归调用，改返回该椅子上的乘客朝向（无人时为 0），即作者本意要取的值。</p>
 *
 * <p>目标类属于可选依赖模组，因此本 mixin 位于 required=false 的独立配置中：
 * 未安装车万女仆时整份配置被跳过，主 jar 不受影响。</p>
 */
@Mixin(EntityChair.class)
public class EntityChairYawFixMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("huajiager");

    private static boolean huajiager$logged;

    @Redirect(method = "getYaw", at = @At(value = "INVOKE",
            target = "Lcom/github/tartaricacid/touhoulittlemaid/entity/item/EntityChair;getYaw()F"))
    private float huajiager$fixSelfRecursion(EntityChair self) {
        if (!huajiager$logged) {
            huajiager$logged = true;
            LOGGER.info("[HuajiAge] Patched Touhou Little Maid EntityChair.getYaw() self-recursion");
        }
        return self.getPassengerYaw();
    }
}
