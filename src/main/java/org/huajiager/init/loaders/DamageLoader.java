package org.huajiager.init.loaders;

import org.huajiager.HuajiAgeRemastered;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/**
 * 自定义伤害类型（DamageType）引用。
 * 1.20.1 中 DamageType 属于动态注册表（datapack 可覆盖），
 * 静态 Registries 类没有 DAMAGE_TYPE 字段，注册通过数据包完成：
 *   src/main/resources/data/huajiager/damage_type/hope_flower.json
 *   src/main/resources/data/huajiager/damage_type/orga_shot.json
 *   src/main/resources/data/huajiager/damage_type/requiem_back.json
 *   src/main/resources/data/huajiager/damage_type/requiem_hit.json
 * msgId 对应翻译键 death.attack.<msgId>：
 * - hope_flower：希望之花处死（无来源）→ death.attack.huajiager.hope_flower
 * - orga_shot：反伤/追踪（来源=玩家）→ death.attack.huajiager.orga.shot(.player)
 * - requiem_back：镇魂曲反伤（来源=玩家）→ death.attack.huajiager.requiem.back
 * - requiem_hit：镇魂曲额外攻击（来源=玩家）→ death.attack.huajiager.requiem.hit
 */
public class DamageLoader {

	public static final RegistryKey<DamageType> HOPE_FLOWER = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "hope_flower"));
	public static final RegistryKey<DamageType> ORGA_SHOT = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "orga_shot"));
	public static final RegistryKey<DamageType> DISC_DEPRIVE = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "disc_deprive"));
	public static final RegistryKey<DamageType> REQUIEM_BACK = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "requiem_back"));
	public static final RegistryKey<DamageType> REQUIEM_HIT = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "requiem_hit"));
	public static final RegistryKey<DamageType> KDJL = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "kdjl"));
	public static final RegistryKey<DamageType> SINGULARITY = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "singularity"));
	public static final RegistryKey<DamageType> SECOND = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "second"));
	public static final RegistryKey<DamageType> STELLA = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "stella"));
	public static final RegistryKey<DamageType> DIO_HIT = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "dio_hit"));
	public static final RegistryKey<DamageType> VOID_BREAK = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "void_break"));
	public static final RegistryKey<DamageType> WAVE_HIT = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "wave_hit"));
	public static final RegistryKey<DamageType> OVERDRIVE_HIT = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "overdrive_hit"));
	public static final RegistryKey<DamageType> SELF_ATTACK = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "self_attack"));
	public static final RegistryKey<DamageType> HOPE_HIT = RegistryKey.of(
			RegistryKeys.DAMAGE_TYPE, Identifier.of(HuajiAgeRemastered.MOD_ID, "hope_hit"));

	/** 希望之花处死：无来源实体，死亡提示 death.attack.huajiager.hope_flower */
	public static DamageSource hopeFlower(Entity context) {
		return new DamageSource(entry(context, HOPE_FLOWER));
	}

	/** ORGA 反击/追踪：来源=攻击者，死亡提示 death.attack.huajiager.orga_shot(.player) */
	public static DamageSource orgaShot(Entity context, Entity source) {
		return new DamageSource(entry(context, ORGA_SHOT), source);
	}

	/** ORGA 追踪兜底：无来源实体，死亡提示 death.attack.huajiager.orga_shot */
	public static DamageSource orgaShotNoSource(Entity context) {
		return new DamageSource(entry(context, ORGA_SHOT));
	}

	/** 镇魂曲反伤：来源=攻击者（玩家），死亡提示 death.attack.huajiager.requiem.back */
	public static DamageSource requiemBack(Entity context, Entity source) {
		return new DamageSource(entry(context, REQUIEM_BACK), source);
	}

	/** 镇魂曲额外攻击：来源=攻击者（玩家），死亡提示 death.attack.huajiager.requiem.hit */
	public static DamageSource requiemHit(Entity context, Entity source) {
		return new DamageSource(entry(context, REQUIEM_HIT), source);
	}

	/** 镇魂曲额外攻击：无来源实体（发动者离线等兜底场景），死亡提示 death.attack.huajiager.requiem.hit */
	public static DamageSource requiemHitNoSource(Entity context) {
		return new DamageSource(entry(context, REQUIEM_HIT));
	}

	/** 可带劲啦：EX面筋棒 flavor3 群伤（无来源），死亡提示 death.attack.huajiager.KeDaiJinLa */
	public static DamageSource kdjl(Entity context) {
		return new DamageSource(entry(context, KDJL));
	}

	/** 白蛇心智剥夺：绝对伤害（无视护甲/无敌/附魔/药水效果，见 minecraft:bypasses_* tag），死亡提示 death.attack.huajiager.disc_deprive */
	public static DamageSource discDeprive(Entity context) {
		return new DamageSource(entry(context, DISC_DEPRIVE));
	}

	/** 特异点压缩伤害：无来源实体，死亡提示 death.attack.huajiager.singularity */
	public static DamageSource singularity(Entity context) {
		return new DamageSource(entry(context, SINGULARITY));
	}

	/** 二向箔：无来源实体，死亡提示 death.attack.huajiager.second */
	public static DamageSource second(Entity context) {
		return new DamageSource(entry(context, SECOND));
	}

	/** 解放大英雄之弓：滑稽之星/自伤/爆炸，无来源实体，死亡提示 death.attack.huajiager.stella */
	public static DamageSource stella(Entity context) {
		return new DamageSource(entry(context, STELLA));
	}

	/** 替身世界（DIO）攻击：无来源实体，死亡提示 death.attack.huajiager.dio.hit */
	public static DamageSource dioHit(Entity context) {
		return new DamageSource(entry(context, DIO_HIT));
	}

	/** 五五开（解放/完全解放）五色之力：无来源实体，死亡提示 death.attack.huajiager.void_break */
	public static DamageSource voidBreak(Entity context) {
		return new DamageSource(entry(context, VOID_BREAK));
	}

	/** 波澜剑/波澜怒涛之刃：无来源实体，死亡提示 death.attack.huajiager.wave_hit */
	public static DamageSource waveHit(Entity context) {
		return new DamageSource(entry(context, WAVE_HIT));
	}

	/** 隐者之紫波纹疾走形态：无来源实体，死亡提示 death.attack.huajiager.overdrive_hit */
	public static DamageSource overdriveHit(Entity context) {
		return new DamageSource(entry(context, OVERDRIVE_HIT));
	}

	/** 命令碟自裁：无来源实体，死亡提示 death.attack.huajiager.self_attack */
	public static DamageSource selfAttack(Entity context) {
		return new DamageSource(entry(context, SELF_ATTACK));
	}

	/** 希望之花替身攻击：无来源实体，死亡提示 death.attack.huajiager.hope_hit */
	public static DamageSource hopeHit(Entity context) {
		return new DamageSource(entry(context, HOPE_HIT));
	}

	private static RegistryEntry<DamageType> entry(Entity context, RegistryKey<DamageType> key) {
		return context.getWorld().getRegistryManager()
				.get(RegistryKeys.DAMAGE_TYPE).getEntry(key).orElseThrow();
	}
}
