package org.huajiager;

import org.huajiager.client.ClientPacketHandlers;
import org.huajiager.client.KeyLoader;
import org.huajiager.client.event.EventArrowRequiemUse;
import org.huajiager.client.event.EventBlanceHelmetKey;
import org.huajiager.client.event.EventBlanceHelmetLeftClick;
import org.huajiager.client.event.EventExglutenburLeftClick;
import org.huajiager.client.event.EventExglutenburModeChange;
import org.huajiager.client.event.EventHeroBowModeChange;
import org.huajiager.client.event.EventHuajiStarSwordLeftClick;
import org.huajiager.client.event.EventOrgaRequiemClient;
import org.huajiager.client.event.EventPlayerLoggedIn;
import org.huajiager.client.event.EventRoadRollerLeftClick;
import org.huajiager.client.event.EventStandHudRender;
import org.huajiager.client.event.EventStandKey;
import org.huajiager.client.event.EventTimeStopView;
import org.huajiager.client.event.ItemTooltipHandlers;
import org.huajiager.client.event.TimeStopPostShader;
import org.huajiager.client.init.sound.HuajiSoundPlayerClient;
import org.huajiager.client.render.entity.RenderDiscCommand;
import org.huajiager.client.render.entity.RenderEmeraldBullet;
import org.huajiager.client.render.entity.RenderFivePower;
import org.huajiager.client.render.entity.RenderHeroArrow;
import org.huajiager.client.render.entity.RenderLordLuWing;
import org.huajiager.client.render.entity.RenderMultiKnife;
import org.huajiager.client.render.entity.RenderOrgaHairKnife;
import org.huajiager.client.render.entity.RenderRoadRoller;
import org.huajiager.client.render.entity.RenderSecondFoil;
import org.huajiager.client.render.entity.RenderSheerHeartAttack;
import org.huajiager.client.render.entity.RenderStandBase;
import org.huajiager.client.render.model.ModelBlanceHelmet;
import org.huajiager.client.render.model.ModelOrgaHair;
import org.huajiager.entity.EmeraldBulletEntity;
import org.huajiager.entity.EntityDiscCommand;
import org.huajiager.entity.EntityFivePower;
import org.huajiager.entity.EntityHeroArrow;
import org.huajiager.entity.EntityLordLuWing;
import org.huajiager.entity.EntityMultiKnife;
import org.huajiager.entity.EntityOrgaHairKnife;
import org.huajiager.entity.EntityRoadRoller;
import org.huajiager.entity.EntitySecondFoil;
import org.huajiager.entity.EntitySheerHeartAttack;
import org.huajiager.init.sound.HuajiSoundPlayer;
import org.huajiager.screen.HuajiBlenderScreen;
import org.huajiager.screen.HuajiPolyfurnaceScreen;
import org.huajiager.screen.MenuLoader;
import org.huajiager.stand.entity.EntityStandBase;
import org.huajiager.init.loaders.ItemLoader;
import org.huajiager.item.ItemBlancedHelmet;
import org.huajiager.item.ItemDiscCommand;
import org.huajiager.item.ItemDiscStand;
import org.huajiager.item.ItemExglutenbur;
import org.huajiager.item.ItemHeroBow;
import org.huajiager.item.ItemHuajiLatiaoSword;
import org.huajiager.item.ItemHuajiStarSword;
import org.huajiager.item.ItemInfiniteCharm;
import org.huajiager.item.ItemMultiKnife;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.ArmorEntityModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

public class HuajiAgeRemasteredClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// Machine GUI screens
		HandledScreens.register(MenuLoader.HUAJI_BLENDER, HuajiBlenderScreen::new);
		HandledScreens.register(MenuLoader.HUAJI_POLYFURNACE, HuajiPolyfurnaceScreen::new);

		EntityRendererRegistry.register(EmeraldBulletEntity.TYPE, RenderEmeraldBullet::new);
		EntityRendererRegistry.register(EntityFivePower.TYPE, RenderFivePower::new);
		EntityRendererRegistry.register(EntityRoadRoller.TYPE_ENTITY, RenderRoadRoller::new);
		EntityRendererRegistry.register(EntitySheerHeartAttack.TYPE, RenderSheerHeartAttack::new);
		EntityRendererRegistry.register(EntityHeroArrow.TYPE, RenderHeroArrow::new);
		EntityRendererRegistry.register(EntityMultiKnife.TYPE, RenderMultiKnife::new);
		EntityRendererRegistry.register(EntityOrgaHairKnife.TYPE, RenderOrgaHairKnife::new);
		EntityRendererRegistry.register(EntityStandBase.TYPE_ENTITY, RenderStandBase::new);
		EntityRendererRegistry.register(EntityDiscCommand.TYPE, RenderDiscCommand::new);
		EntityRendererRegistry.register(EntitySecondFoil.TYPE, RenderSecondFoil::new);
		EntityRendererRegistry.register(EntityLordLuWing.TYPE, RenderLordLuWing::new);

		ClientPacketHandlers.register();

		KeyLoader.register();

		EventStandKey.register();
		EventOrgaRequiemClient.register();
		EventRoadRollerLeftClick.register();
		EventHeroBowModeChange.register();
		EventHuajiStarSwordLeftClick.register();
		EventBlanceHelmetLeftClick.register();
		EventBlanceHelmetKey.register();
		EventExglutenburLeftClick.register();
		EventExglutenburModeChange.register();
		EventArrowRequiemUse.register();
		EventPlayerLoggedIn.register();
		EventStandHudRender.register();
		EventTimeStopView.register();
		TimeStopPostShader.register();
		ItemTooltipHandlers.register();

		// Inject real client sound player implementation
		HuajiSoundPlayer.setClientSoundPlayer(HuajiSoundPlayerClient.INSTANCE);

		// Disc item model predicates (stand discs + command discs)
		registerDiscPredicates();

		// 辣条剑形态切换：NBT "hot" -> 模型谓词 "burst"（与 huaji_latiao_sword.json overrides 保持一致）
		ModelPredicateProviderRegistry.register(ItemLoader.huajiLatiaoSword,
				Identifier.of("minecraft", "burst"),
				(stack, world, entity, seed) -> ItemHuajiLatiaoSword.isOpen(stack) ? 1.0f : 0.0f);

		// 多刀发光切换：NBT "light" -> 模型谓词 "light"（multi_knife.json overrides 0/1 -> multi_knife_n/shiny）
		ModelPredicateProviderRegistry.register(ItemLoader.multiKnife,
				Identifier.of("minecraft", "light"),
				(stack, world, entity, seed) -> ItemMultiKnife.getLight(stack) ? 1.0f : 0.0f);

		// 滑稽之星剑 burst 切换：NBT "open" -> 模型谓词 "burst"（huaji_star_sword.json overrides 0/1 -> star_sword_0/1）
		ModelPredicateProviderRegistry.register(ItemLoader.huajiStarSword,
				Identifier.of("minecraft", "burst"),
				(stack, world, entity, seed) -> ItemHuajiStarSword.isOpen(stack) ? 1.0f : 0.0f);

		// EX面筋棒 flavor 切换：NBT "flavor"(0~3) -> 模型谓词 flavor_1/2/3（0/1 布尔）
		// 1.20.1 override 匹配为"谓词值 >= override 值即命中第一个"，若用单一递增谓词
		// flavor=1/2/3，则 flavor=2/3 会误命中 flavor=1 的 override，贴图停在第一形态。		// 改独立布尔谓词精确匹配（exglutenbur.json overrides 同步为 flavor_1/2/3 -> exglutenbur_1/2/3）
		ModelPredicateProviderRegistry.register(ItemLoader.exglutenbur,
				Identifier.of("minecraft", "flavor_1"),
				(stack, world, entity, seed) -> ItemExglutenbur.flavor(stack) == 1 ? 1.0f : 0.0f);
		ModelPredicateProviderRegistry.register(ItemLoader.exglutenbur,
				Identifier.of("minecraft", "flavor_2"),
				(stack, world, entity, seed) -> ItemExglutenbur.flavor(stack) == 2 ? 1.0f : 0.0f);
		ModelPredicateProviderRegistry.register(ItemLoader.exglutenbur,
				Identifier.of("minecraft", "flavor_3"),
				(stack, world, entity, seed) -> ItemExglutenbur.flavor(stack) == 3 ? 1.0f : 0.0f);

		// 大英雄之弓：拉弓/蓄力/解放模型谓词（与 hero_bow.json overrides 顺序一致，
		// 语义 pulling/pull 三段蓄力 + burst 解放形态）
		registerHeroBowPredicates();

		// 无限耐久护身符 orga 切换：NBT "orga" -> 模型谓词 "orga"（infinite_charm.json overrides
		// 0/1 -> infinite_charm_n/orga_flag； addPropertyOverride("orga") 亦仅在实体
		// 渲染时生效，物品栏/实体外渲染 entity==null 时回落 0 显示 3D 护身符本体）
		ModelPredicateProviderRegistry.register(ItemLoader.infiniteCharm,
				Identifier.of("minecraft", "orga"),
				(stack, world, entity, seed) ->
						entity != null && ItemInfiniteCharm.isOrga(stack) ? 1.0f : 0.0f);

		// 奥尔加发型：注册 ArmorRenderer，用  ModelOrgaHair 同款模型树渲染为头发
		// （而非默认头盔盒），贴图 textures/models/armor/orga.png
		registerOrgaHairRenderer();

		// 五五开头盔：注册 ArmorRenderer，用标准护甲模型（64x64）渲染头部
		registerBlanceHelmetRenderer();
	}

	/** 大英雄之弓模型谓词（minecraft:pulling / minecraft:pull / minecraft:burst）。 */
	private static void registerHeroBowPredicates() {
		ModelPredicateProviderRegistry.register(ItemLoader.heroBow,
				Identifier.of("minecraft", "pulling"),
				(stack, world, entity, seed) ->
						entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0f : 0.0f);
		ModelPredicateProviderRegistry.register(ItemLoader.heroBow,
				Identifier.of("minecraft", "pull"),
				(stack, world, entity, seed) -> {
					if (entity == null) {
						return 0.0f;
					}
					return entity.getActiveItem() != stack ? 0.0f : BowItem.getPullProgress(entity.getItemUseTime());
				});
		ModelPredicateProviderRegistry.register(ItemLoader.heroBow,
				Identifier.of("minecraft", "burst"),
				(stack, world, entity, seed) -> ItemHeroBow.isOpen(stack) ? 1.0f : 0.0f);
	}

	/** 奥尔加发型 ArmorRenderer：仅渲染头部，头发盒体挂在 head 下（同 ModelOrgaHair）。 */
	private static void registerOrgaHairRenderer() {
		ArmorRenderer.register((MatrixStack matrices, VertexConsumerProvider vertexConsumers,
								ItemStack stack, LivingEntity entity, EquipmentSlot slot,
								int light, BipedEntityModel<LivingEntity> contextModel) -> {
			ArmorEntityModel<LivingEntity> model = ModelOrgaHair.createHairAttachedArmorModel();
			// 姿态同步方向：vanilla 语义是 contextModel（玩家模型）→ armorModel，反写会把玩家模型姿态覆盖成默认站姿
			contextModel.copyBipedStateTo(model);
			//  ModelOrgaHair 仅 HEAD 槽显示 head 盒体（含 10 段头发），其余部位隐藏
			model.setVisible(false);
			model.head.visible = true;
			VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(ModelOrgaHair.TEXTURE));
			model.render(matrices, vc, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);
		}, ItemLoader.orgaHair);
	}

	/** 平衡头盔 ArmorRenderer：头部渲染外凸盔体。	 *  lord 态显示头管；open+active 发光眼带/头管。	 *  active 态背部渲染挂者核心物品。Lord.Lu 翅膀已实体化（EntityLordLuWing）
	 *  由独立实体渲染器 RenderLordLuWing 渲染，此处不再绘制（避免双重渲染）。 */
	private static void registerBlanceHelmetRenderer() {
		ArmorRenderer.register((MatrixStack matrices, VertexConsumerProvider vertexConsumers,
								ItemStack stack, LivingEntity entity, EquipmentSlot slot,
								int light, BipedEntityModel<LivingEntity> contextModel) -> {
			ModelBlanceHelmet model = ModelBlanceHelmet.get();
			// 姿态同步方向：vanilla 语义是 contextModel（玩家模型）→ armorModel
			contextModel.copyBipedStateTo(model);
			model.setVisible(false);
			model.head.visible = true;

			boolean active = ItemBlancedHelmet.isActive(stack);
			boolean open = ItemBlancedHelmet.isOpen(stack);
			boolean lord = ItemBlancedHelmet.isLord(stack);

			// 头部盔体：lord 态显示 tubes_b/tubes_y
			model.setTubesVisible(lord);
			VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(ModelBlanceHelmet.TEXTURE));
			model.render(matrices, vc, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f);

			// 发光：open+active 且非潜行时以 head 姿态叠加渲染 eyes 与 tubes_b（全亮）
			if (open && active && !entity.isSneaking()) {
				matrices.push();
				model.head.rotate(matrices);
				VertexConsumer glowVc = vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(ModelBlanceHelmet.TEXTURE));
				model.renderEyesTubes(matrices, glowVc);
				matrices.pop();
			}

			// 背部挂者核心：active 态渲染。
			// 关键：不调用 ItemRenderer.renderItem——其内部走独立的 item RenderLayer
			// （getItemEntityTranslucentCull），与翅膀的 entityTranslucentEmissive 分属
			// 不同 buffer，层间 flush 顺序不可控，实测物品会盖住翅膀。			// 改为手动把 lordCore BakedModel 的 quads 直接写入与翅膀相同的
			// entityTranslucentEmissive buffer（纹理用方块图集，quad UV 直接匹配）：
			// 同 buffer 严格按顶点提交顺序绘制，核心先画、翅膀后画，翅膀必然覆盖核心。
			if (active) {
				matrices.push();
				matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(180));
				matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotationDegrees(180));
				matrices.translate(0.0, 0.4, -0.25);
				if (entity.isInSneakingPose()) {
					matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_X.rotation(-0.5f));
				}
				net.minecraft.client.render.item.ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
				net.minecraft.client.render.model.BakedModel baked = itemRenderer.getModel(
						ItemLoader.lordCore.getDefaultStack(), null, null, entity.getId());
				baked.getTransformation().getTransformation(
						net.minecraft.client.render.model.json.ModelTransformationMode.FIXED).apply(false, matrices);
				matrices.translate(-0.5F, -0.5F, -0.5F);
				VertexConsumer coreVc = vertexConsumers.getBuffer(
						RenderLayer.getEntityTranslucentEmissive(net.minecraft.client.texture.SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
				net.minecraft.util.math.random.Random random = net.minecraft.util.math.random.Random.create();
				MatrixStack.Entry entry = matrices.peek();
				for (net.minecraft.util.math.Direction direction : net.minecraft.util.math.Direction.values()) {
					random.setSeed(42L);
					for (net.minecraft.client.render.model.BakedQuad quad : baked.getQuads(null, direction, random)) {
						coreVc.quad(entry, quad, 1.0F, 1.0F, 1.0F, 0xF000F0, 0xFFFFFF);
					}
				}
				random.setSeed(42L);
				for (net.minecraft.client.render.model.BakedQuad quad : baked.getQuads(null, null, random)) {
					coreVc.quad(entry, quad, 1.0F, 1.0F, 1.0F, 0xF000F0, 0xFFFFFF);
				}
				matrices.pop();
			}
		}, ItemLoader.blanceHelmet);
	}

	private static void registerDiscPredicates() {
		registerCommandDiscPredicate("command_explosion", "explosion");
		registerCommandDiscPredicate("command_move_up", "move_up");
		registerCommandDiscPredicate("command_self_attack", "self_attack");

		registerStandDiscPredicate("stand_the_world", "the_world");
		registerStandDiscPredicate("stand_star_platinum", "star_platinum");
		registerStandDiscPredicate("stand_hierophant_green", "hierophant_green");
		registerStandDiscPredicate("stand_orga_requiem", "orga_requiem");
		registerStandDiscPredicate("stand_killer_queen", "killer_queen");
		registerStandDiscPredicate("stand_crazy_diamond", "crazy_diamond");
		registerStandDiscPredicate("stand_hermit_purple", "hermit_purple");
		registerStandDiscPredicate("stand_white_snake", "white_snake");
	}

	private static void registerCommandDiscPredicate(String key, String type) {
		ModelPredicateProviderRegistry.register(ItemLoader.discCommand,
				Identifier.of("minecraft", key),
				(stack, world, entity, seed) ->
						ItemDiscCommand.getCommandType(stack).equals(type) ? 1.0f : 0.0f);
	}

	private static void registerStandDiscPredicate(String key, String shortId) {
		String fullId = HuajiAgeRemastered.MOD_ID + ":" + shortId;
		ModelPredicateProviderRegistry.register(ItemLoader.discStand,
				Identifier.of("minecraft", key),
				(stack, world, entity, seed) -> {
					String id = ItemDiscStand.getStandId(stack);
					if (id == null || id.isEmpty()) {
						return 0.0f;
					}
					return id.equals(shortId) || id.equals(fullId) ? 1.0f : 0.0f;
				});
	}
}
