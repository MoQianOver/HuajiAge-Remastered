package org.huajiager.init.loaders;

import org.huajiager.HuajiAgeRemastered;
import org.huajiager.item.ItemArrowRequiem;
import org.huajiager.item.ItemArrowStand;
import org.huajiager.item.ItemBakingGluten;
import org.huajiager.item.ItemBlackCar;
import org.huajiager.item.ItemBlancedHelmet;
import org.huajiager.item.ItemDisc;
import org.huajiager.item.ItemDiscCommand;
import org.huajiager.item.ItemDiscMemory;
import org.huajiager.item.ItemDiscStand;
import org.huajiager.item.ItemTarot;
import org.huajiager.item.ItemExpensiveCamera;
import org.huajiager.item.ItemDiscMind;
import org.huajiager.item.ItemDioBread;
import org.huajiager.item.ItemEggRice;
import org.huajiager.item.ItemEggRiceU;
import org.huajiager.item.ItemEther;
import org.huajiager.item.ItemEtherCircumfluxBoard;
import org.huajiager.item.ItemExglutenbur;
import org.huajiager.item.ItemExpendedView;
import org.huajiager.item.ItemFlashFlour;
import org.huajiager.item.ItemHopeElement;
import org.huajiager.item.ItemHeroBow;
import org.huajiager.item.ItemHopeFlower;
import org.huajiager.item.ItemKillerQueenTrigger;
import org.huajiager.item.ItemRoadRoller;
import org.huajiager.item.ItemHuaji;
import org.huajiager.item.ItemHuajiArmor;
import org.huajiager.item.ItemHuajiFragment;
import org.huajiager.item.ItemHuajiIngot;
import org.huajiager.item.ItemHuajiLatiaoSword;
import org.huajiager.item.ItemHuajiStar;
import org.huajiager.item.ItemHuajiStarSword;
import org.huajiager.item.ItemMultiKnife;
import org.huajiager.item.ItemHuajiStarPoly;
import org.huajiager.item.ItemHuajiStarSky;
import org.huajiager.item.ItemHuajiStarUniverse;
import org.huajiager.item.ItemHuajiSword;
import org.huajiager.item.ItemInfiniteCharm;
import org.huajiager.item.ItemLordCore;
import org.huajiager.item.ItemLordKey;
import org.huajiager.item.ItemNeutronStarFragment;
import org.huajiager.item.ItemOrgaArmor;
import org.huajiager.item.ItemOrgaFlag;
import org.huajiager.item.ItemOrgaHair;
import org.huajiager.item.ItemOrgaHairKnife;
import org.huajiager.item.ItemOrgaRequiem;
import org.huajiager.item.ItemOrgaRunning;
import org.huajiager.item.ItemRawGluten;
import org.huajiager.item.ItemRedstoneDruse;
import org.huajiager.item.ItemReoCherry;
import org.huajiager.item.ItemSecondFoil;
import org.huajiager.item.ItemSecondFoilEntity;
import org.huajiager.item.ItemSingularity;
import org.huajiager.item.ItemWaveCrystal;
import org.huajiager.item.ItemWaveKnife;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * 物品注册器。
 *
 * Fabric 侧统一在 Registry.ITEM 注册四个物品，并自建一个创造模式标签页
 * （替代原 CreativeTabLoader.tabJo / tabhuaji）收纳全部物品及命令飞盘类型变体。
 */
public class ItemLoader {

	public static Item disc;
	public static Item discCommand;
	public static Item secondFoil;
	public static Item secondFoilEntity;
	public static Item expendedView;

	public static Item huaji;
	public static Item huajiSword;
	public static Item huajiFragment;
	public static Item huajiIngot;
	public static Item neutronStarFragment;
	public static Item huajiStar;
	public static Item huajiStarSky;
	public static Item huajiStarUniverse;
	public static Item ether;
	public static Item etherCircumfluxBoard;
	public static Item redstoneDruse;
	public static Item flashFlour;
	public static Item rawGluten;
	public static Item bakingGluten;
	public static Item eggRice;
	public static Item eggRiceU;
	public static Item huajiStarPoly;
	public static Item lordCore;
	public static Item lordKey;
	public static Item hopeElement;
	public static Item hopeFlower;

	public static Item huajiLatiaoSword;
	public static Item huajiHelmet;
	public static Item huajiChestplate;
	public static Item huajiLeggings;
	public static Item huajiBoots;
	public static Item orgaHair;
	public static Item orgaChestplate;
	public static Item orgaLeggings;
	public static Item orgaBoots;
	public static Item orgaFlag;
	public static Item infiniteCharm;
	public static Item discMind;
	public static Item discMemory;
	public static Item dioBread;
	public static Item reoCherry;
	public static Item singularity;
	public static Item expensiveCamera;
	public static Item arrowRequiem;
	public static Item arrowStand;
	public static Item waveKnife;
	public static Item waveCrystal;
	public static Item orgaRunning;
	public static Item orgaRequiem;
	public static Item orgaHairKnife;

	public static Item tarot;
	public static Item discStand;

	public static Item heroBow;
	public static Item roadRoller;
	public static Item blackCar;
	public static Item killerQueenTrigger;
	public static Item multiKnife;
	public static Item huajiStarSword;
	public static Item blanceHelmet;
	public static Item exglutenbur;

	public static void register() {
		String modId = HuajiAgeRemastered.MOD_ID;

		disc = new ItemDisc();
		discCommand = new ItemDiscCommand();
		secondFoil = new ItemSecondFoil();
		secondFoilEntity = new ItemSecondFoilEntity();
		expendedView = new ItemExpendedView();

		huaji = new ItemHuaji();
		huajiSword = new ItemHuajiSword();
		huajiFragment = new ItemHuajiFragment();
		huajiIngot = new ItemHuajiIngot();
		neutronStarFragment = new ItemNeutronStarFragment();
		huajiStar = new ItemHuajiStar();
		huajiStarSky = new ItemHuajiStarSky();
		huajiStarUniverse = new ItemHuajiStarUniverse();
		ether = new ItemEther();
		etherCircumfluxBoard = new ItemEtherCircumfluxBoard();
		redstoneDruse = new ItemRedstoneDruse();
		flashFlour = new ItemFlashFlour();
		rawGluten = new ItemRawGluten();
		bakingGluten = new ItemBakingGluten();
		eggRice = new ItemEggRice();
		eggRiceU = new ItemEggRiceU();
		huajiStarPoly = new ItemHuajiStarPoly();
		lordCore = new ItemLordCore();
		lordKey = new ItemLordKey();
		hopeElement = new ItemHopeElement();
		hopeFlower = new ItemHopeFlower();

		huajiLatiaoSword = new ItemHuajiLatiaoSword();
		huajiHelmet = new ItemHuajiArmor.Helmet();
		huajiChestplate = new ItemHuajiArmor.Chestplate();
		huajiLeggings = new ItemHuajiArmor.Leggings();
		huajiBoots = new ItemHuajiArmor.Boots();
		orgaHair = new ItemOrgaHair();
		orgaChestplate = new ItemOrgaArmor.Chestplate();
		orgaLeggings = new ItemOrgaArmor.Leggings();
		orgaBoots = new ItemOrgaArmor.Boots();
		orgaFlag = new ItemOrgaFlag();
		infiniteCharm = new ItemInfiniteCharm();
		discMind = new ItemDiscMind();
		discMemory = new ItemDiscMemory();
		dioBread = new ItemDioBread();
		reoCherry = new ItemReoCherry();
		singularity = new ItemSingularity();
		expensiveCamera = new ItemExpensiveCamera();
		arrowRequiem = new ItemArrowRequiem();
		arrowStand = new ItemArrowStand();
		waveKnife = new ItemWaveKnife();
		waveCrystal = new ItemWaveCrystal();

		orgaRunning = new ItemOrgaRunning();
		orgaRequiem = new ItemOrgaRequiem();
		orgaHairKnife = new ItemOrgaHairKnife();

		// 替身测试前置道具：命运的塔罗牌 + 替身 Disc
		tarot = new ItemTarot();
		discStand = new ItemDiscStand();

		// 压路机 + 大英雄之弓
		heroBow = new ItemHeroBow();
		roadRoller = new ItemRoadRoller();
		blackCar = new ItemBlackCar();

		// 点赞（杀手皇后触发器），攻击生物由 EventKillerQueen 自动发放
		killerQueenTrigger = new ItemKillerQueenTrigger();

		// 多刀 + 滑稽之星剑
		multiKnife = new ItemMultiKnife();
		huajiStarSword = new ItemHuajiStarSword();

		// ww头盔（五五开头盔）+ EX面筋棒
		blanceHelmet = new ItemBlancedHelmet();
		exglutenbur = new ItemExglutenbur();

		Registry.register(Registries.ITEM, Identifier.of(modId, "disc_command"), discCommand);
		Registry.register(Registries.ITEM, Identifier.of(modId, "second_foil"), secondFoil);
		Registry.register(Registries.ITEM, Identifier.of(modId, "second_foil_entity"), secondFoilEntity);
		Registry.register(Registries.ITEM, Identifier.of(modId, "expended_view"), expendedView);

		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji"), huaji);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_sword"), huajiSword);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_fragment"), huajiFragment);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_ingot"), huajiIngot);
		Registry.register(Registries.ITEM, Identifier.of(modId, "neutron_star_fragment"), neutronStarFragment);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_star"), huajiStar);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_star_sky"), huajiStarSky);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_star_universe"), huajiStarUniverse);
		Registry.register(Registries.ITEM, Identifier.of(modId, "ether"), ether);
		Registry.register(Registries.ITEM, Identifier.of(modId, "ether_circumflux_board"), etherCircumfluxBoard);
		Registry.register(Registries.ITEM, Identifier.of(modId, "redstone_druse"), redstoneDruse);
		Registry.register(Registries.ITEM, Identifier.of(modId, "flash_flour"), flashFlour);
		Registry.register(Registries.ITEM, Identifier.of(modId, "raw_gluten"), rawGluten);
		Registry.register(Registries.ITEM, Identifier.of(modId, "baking_gluten"), bakingGluten);
		Registry.register(Registries.ITEM, Identifier.of(modId, "egg_rice"), eggRice);
		Registry.register(Registries.ITEM, Identifier.of(modId, "egg_rice_u"), eggRiceU);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_star_poly"), huajiStarPoly);
		Registry.register(Registries.ITEM, Identifier.of(modId, "lord_core"), lordCore);
		Registry.register(Registries.ITEM, Identifier.of(modId, "lord_key"), lordKey);
		Registry.register(Registries.ITEM, Identifier.of(modId, "hope_element"), hopeElement);
		Registry.register(Registries.ITEM, Identifier.of(modId, "hope_flower"), hopeFlower);

		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_latiao_sword"), huajiLatiaoSword);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_helmet"), huajiHelmet);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_chestplate"), huajiChestplate);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_leggings"), huajiLeggings);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_boots"), huajiBoots);
		Registry.register(Registries.ITEM, Identifier.of(modId, "orga_hair"), orgaHair);
		Registry.register(Registries.ITEM, Identifier.of(modId, "orga_chestplate"), orgaChestplate);
		Registry.register(Registries.ITEM, Identifier.of(modId, "orga_leggings"), orgaLeggings);
		Registry.register(Registries.ITEM, Identifier.of(modId, "orga_boots"), orgaBoots);
		Registry.register(Registries.ITEM, Identifier.of(modId, "orga_flag"), orgaFlag);
		Registry.register(Registries.ITEM, Identifier.of(modId, "infinite_charm"), infiniteCharm);
		Registry.register(Registries.ITEM, Identifier.of(modId, "disc_mind"), discMind);
		Registry.register(Registries.ITEM, Identifier.of(modId, "disc_memory"), discMemory);
		Registry.register(Registries.ITEM, Identifier.of(modId, "dio_bread"), dioBread);
		Registry.register(Registries.ITEM, Identifier.of(modId, "reo_cherry"), reoCherry);
		Registry.register(Registries.ITEM, Identifier.of(modId, "singularity"), singularity);
		Registry.register(Registries.ITEM, Identifier.of(modId, "expensive_camera"), expensiveCamera);
		Registry.register(Registries.ITEM, Identifier.of(modId, "arrow_requiem"), arrowRequiem);
		Registry.register(Registries.ITEM, Identifier.of(modId, "arrow_stand"), arrowStand);
		Registry.register(Registries.ITEM, Identifier.of(modId, "wave_knife"), waveKnife);
		Registry.register(Registries.ITEM, Identifier.of(modId, "wave_crystal"), waveCrystal);
		Registry.register(Registries.ITEM, Identifier.of(modId, "orga_running"), orgaRunning);
		Registry.register(Registries.ITEM, Identifier.of(modId, "orga_requiem"), orgaRequiem);
		Registry.register(Registries.ITEM, Identifier.of(modId, "orga_hair_knife"), orgaHairKnife);
		Registry.register(Registries.ITEM, Identifier.of(modId, "tarot"), tarot);
		Registry.register(Registries.ITEM, Identifier.of(modId, "disc"), disc);
		Registry.register(Registries.ITEM, Identifier.of(modId, "disc_stand"), discStand);
		Registry.register(Registries.ITEM, Identifier.of(modId, "hero_bow"), heroBow);
		Registry.register(Registries.ITEM, Identifier.of(modId, "road_roller"), roadRoller);
		Registry.register(Registries.ITEM, Identifier.of(modId, "black_car"), blackCar);
		Registry.register(Registries.ITEM, Identifier.of(modId, "killer_queen_trigger"), killerQueenTrigger);
		Registry.register(Registries.ITEM, Identifier.of(modId, "multi_knife"), multiKnife);
		Registry.register(Registries.ITEM, Identifier.of(modId, "huaji_star_sword"), huajiStarSword);
		Registry.register(Registries.ITEM, Identifier.of(modId, "blance_helmet"), blanceHelmet);
		Registry.register(Registries.ITEM, Identifier.of(modId, "exglutenbur"), exglutenbur);

		// 创造标签页：对齐 CreativeTabLoader（tabhuaji + tabJo，不含 tabVehicle）
		// tabhuaji 主组：图标 huaji，按 ItemLoader 注册顺序；未注册物品与载具不入组
		ItemGroup groupHuaji = FabricItemGroup.builder()
				.icon(() -> new ItemStack(huaji))
				.displayName(Text.translatable("itemGroup.huajiager.huaji"))
				.entries((context, entries) -> {
					entries.add(secondFoil);
					entries.add(expendedView);
					entries.add(huaji);
					entries.add(huajiFragment);
					entries.add(neutronStarFragment);
					entries.add(huajiStar);
					entries.add(huajiStarSky);
					entries.add(huajiStarUniverse);
					entries.add(huajiStarPoly);
					entries.add(eggRice);
					entries.add(eggRiceU);
					entries.add(huajiIngot);
					entries.add(huajiSword);
					entries.add(huajiStarSword);
					entries.add(huajiHelmet);
					entries.add(huajiChestplate);
					entries.add(huajiLeggings);
					entries.add(huajiBoots);
					entries.add(blanceHelmet);
					entries.add(huajiLatiaoSword);
					entries.add(ether);
					entries.add(redstoneDruse);
					entries.add(heroBow);
					entries.add(exglutenbur);
					entries.add(flashFlour);
					entries.add(rawGluten);
					entries.add(bakingGluten);
					entries.add(orgaHair);
					entries.add(orgaChestplate);
					entries.add(orgaLeggings);
					entries.add(orgaBoots);
					entries.add(orgaRunning);
					entries.add(orgaHairKnife);
					entries.add(hopeElement);
					entries.add(hopeFlower);
					entries.add(waveCrystal);
					entries.add(waveKnife);
					entries.add(infiniteCharm);
					entries.add(blackCar);
					entries.add(etherCircumfluxBoard);
					entries.add(lordCore);
					entries.add(lordKey);
				})
				.build();
		Registry.register(Registries.ITEM_GROUP, Identifier.of(modId, "huaji"), groupHuaji);

		// tabJo 组：图标 tarot，按 ItemLoader 注册顺序
		ItemGroup groupJo = FabricItemGroup.builder()
				.icon(() -> new ItemStack(tarot))
				.displayName(Text.translatable("itemGroup.huajiager.jo"))
				.entries((context, entries) -> {
					entries.add(arrowRequiem);
					entries.add(orgaRequiem);
					entries.add(dioBread);
					// 多刀双形态（light=false 普通 / light=true 8848 至尊折叠刀）
					entries.add(ItemMultiKnife.setLight(new ItemStack(multiKnife), false));
					entries.add(ItemMultiKnife.setLight(new ItemStack(multiKnife), true));
					entries.add(roadRoller);
					// 塔罗牌（存储/卸载替身）+ 各原生替身 Disc 变体
					// 奥尔加镇魂曲（停不下来的镇魂曲）物品组只放镇魂曲变体
					entries.add(tarot);
					entries.add(discStand);
					for (org.huajiager.stand.instance.StandBase sb : org.huajiager.init.loaders.StandLoader.STAND_LIST) {
						if (sb.getName().equals(org.huajiager.init.loaders.StandLoader.ORGA_REQUIEM.getName())) {
							entries.add(ItemDiscStand.createDisc(new ItemStack(discStand), sb.getName(), 3));
							continue;
						}
						entries.add(ItemDiscStand.createDisc(new ItemStack(discStand), sb.getName(), 0));
						entries.add(ItemDiscStand.createDisc(new ItemStack(discStand), sb.getName(), 1));
					}
					entries.add(expensiveCamera);
					entries.add(singularity);
					entries.add(arrowStand);
					entries.add(killerQueenTrigger);
					entries.add(reoCherry);
					entries.add(discMind);
					entries.add(discMemory);
					// 命令飞盘本体 + 各命令类型变体
					entries.add(discCommand);
					for (ItemDiscCommand.TYPES_COMMAND t : ItemDiscCommand.TYPES_COMMAND.values()) {
						ItemStack s = new ItemStack(discCommand);
						ItemDiscCommand.setCommandType(s, t.getCommand());
						entries.add(s);
					}
				})
				.build();
		Registry.register(Registries.ITEM_GROUP, Identifier.of(modId, "jo"), groupJo);
	}
}
