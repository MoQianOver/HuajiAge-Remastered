package org.huajiager.init;

import org.huajiager.HuajiAgeRemastered;

import net.minecraft.util.Identifier;

/**
 * 常量定义， 。
 * 注：StandTex 原实现引用 StandLoader 的静态实例取贴图路径，此处先以字面量等价替换
 * （路径与源 mod 一致），待 Stand 系统完成后可恢复引用。
 */
public class HuajiConstant {

	public static class Tags {
		// Util
		public static final String PLAYER_UUID = HuajiAgeRemastered.MOD_ID + "." + "player_uuid";
		public static final String PLAYER_NAME = HuajiAgeRemastered.MOD_ID + "." + "player_name";
		// Stand Events
		public static final String SINGULARITY = HuajiAgeRemastered.MOD_ID + "." + "singularity";
		// Dio Bread
		public static final String TIME_STOP = HuajiAgeRemastered.MOD_ID + "." + "time_stop";
		public static final String TIME_STOP_RANGE = HuajiAgeRemastered.MOD_ID + "." + "time_stop_range";
		public static final String THE_WORLD = HuajiAgeRemastered.MOD_ID + "." + "the_world";
		public static final String THE_WORLD_RECORD = HuajiAgeRemastered.MOD_ID + "." + "the_world_record";
		public static final String DIO_FLAG = HuajiAgeRemastered.MOD_ID + "." + "dio_flag";
		public static final String DIO_HIT = HuajiAgeRemastered.MOD_ID + "." + "dio_hit";
		public static final String DIO_HIT_EXTRA = HuajiAgeRemastered.MOD_ID + "." + "dio_hit_extra";

		public static final int THE_WORLD_TIME = 9 * 20;

		// Requiem
		public static final String REQUIEM = HuajiAgeRemastered.MOD_ID + "." + "requiem";
		public static final String REQUIEM_OWNER = HuajiAgeRemastered.MOD_ID + "." + "requiem_owner";
	}

	public static class BuffTags {
		public static final String TIME_STOP = HuajiAgeRemastered.MOD_ID + "." + "buff" + "." + "time_stop";
		public static final String OVER_DRIVE = HuajiAgeRemastered.MOD_ID + "." + "buff" + "." + "overdrive";
	}

	// Stand type
	public static class StandType {
		public static final String STAND_TYPE = HuajiAgeRemastered.MOD_ID + "." + "stand_type";
		public static final String STAND_THE_WORLD = "stand" + "." + HuajiAgeRemastered.MOD_ID + "." + "the_world";
		public static final String STAND_STAR_PLATINUM = "stand" + "." + HuajiAgeRemastered.MOD_ID + "." + "star_platinum";
		public static final String STAND_HIEROPANT_GREEN = "stand" + "." + HuajiAgeRemastered.MOD_ID + "." + "hierophant_green";
		public static final String STAND_ORGA_REQUIEM = "stand" + "." + HuajiAgeRemastered.MOD_ID + "." + "orga_requiem";
		public static final String STAND_KILLER_QUEEN = "stand" + "." + HuajiAgeRemastered.MOD_ID + "." + "killer_queen";
		public static final String STAND_EMPTY = "stand" + "." + HuajiAgeRemastered.MOD_ID + "." + "empty";
	}

	public static class StandTex {
		public static final Identifier TEXTRUE_THE_WORLD = Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/entity/entity_the_world_default.png");
		public static final Identifier TEXTRUE_STAR_PLATINUM = Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/entity/entity_star_platinum_default.png");
		public static final Identifier TEXTRUE_HIEROPANT_GREEN = Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/entity/entity_hierophant_green_default.png");
		public static final Identifier TEXTRUE_ORGA_REQUIEM = Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/entity/entity_orga_requiem_default.png");
		public static final Identifier TEXTRUE_KILLER_QUEEN = Identifier.of(HuajiAgeRemastered.MOD_ID, "textures/entity/entity_killer_queen_default.png");
	}

	// DamageSource
	public static class DamageSource {
		public static final String SECOND = HuajiAgeRemastered.MOD_ID + "." + "second";
		public static final String STELLA = HuajiAgeRemastered.MOD_ID + "." + "stella";
		public static final String KDJL = HuajiAgeRemastered.MOD_ID + "." + "KeDaiJinLa";
		public static final String ORGA_SHOT = HuajiAgeRemastered.MOD_ID + "." + "orga.shot";
		public static final String HOPE_FLOWER = HuajiAgeRemastered.MOD_ID + "." + "hope_flower";
		public static final String REQUIEM_BACK = HuajiAgeRemastered.MOD_ID + "." + "requiem.back";
		public static final String REQUIEM_DAMAGE = HuajiAgeRemastered.MOD_ID + "." + "requiem.hit";
		public static final String STAND_PUNCH_DAMAGE = HuajiAgeRemastered.MOD_ID + "." + "dio.hit";
		public static final String SINGULARITY_DAMAGE = HuajiAgeRemastered.MOD_ID + "." + "singularity";
		public static final String VOID_BREAK = HuajiAgeRemastered.MOD_ID + "." + "void_break";
		public static final String WAVE_HIT = HuajiAgeRemastered.MOD_ID + "." + "wave_hit";
		public static final String OVERDRIVE_HIT = HuajiAgeRemastered.MOD_ID + "." + "overdrive_hit";
		public static final String HOPE_HIT = HuajiAgeRemastered.MOD_ID + "." + "hope_hit";
		public static final String DISC_DEPRIVE = HuajiAgeRemastered.MOD_ID + "." + "disc_deprive";
		public static final String SELF_ATTACK = HuajiAgeRemastered.MOD_ID + "." + "self_attack";
	}

	public static class StandModels {
		public static final String DEFAULT_MODEL_ID = "huajiager:the_world_default";
	}
}
