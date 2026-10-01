package org.huajiager.init.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.huajiager.HuajiAgeRemastered;

import java.util.ArrayList;
import java.util.List;

/**
 * 声音注册器（ SoundLoader）。
 *
 * 修复点：原实现把注册动作放在静态字段初始化（<clinit>）里，
 * 运行时首次引用 SoundLoader（如 ItemDioBread.finishUsing）才触发类加载，
 * 此时 sound_event 注册表已冻结 -> IllegalStateException: Registry is already frozen。
 * 现改为：静态字段仅用 SoundEvent.of(id) 构建事件对象（不触注册表），
 * 真正的 Registry.register 收敛到显式 init()，在 mod 入口 onInitialize 中
 * 于注册表冻结前调用，保证只注册一次且避开 <clinit> 副作用。
 * 音效语义不变：SoundEvent 常量名、注册 id、sounds.json 对应关系均未改动。
 */
public class SoundLoader {
    private static final List<SoundEvent> SOUND_LIST = new ArrayList<>();

    public static final SoundEvent ENERGY_HIT = SoundEvent.of(id("energyhit"));
    public static final SoundEvent CHARGE = SoundEvent.of(id("charge"));
    public static final SoundEvent WAVE1 = SoundEvent.of(id("wave_01"));
    public static final SoundEvent STELLA = SoundEvent.of(id("stella"));
    public static final SoundEvent EXGLUTENBUR_1 = SoundEvent.of(id("exglutenbur_flavor1"));
    public static final SoundEvent EXGLUTENBUR_2 = SoundEvent.of(id("exglutenbur_flavor2"));
    public static final SoundEvent EXGLUTENBUR_3 = SoundEvent.of(id("exglutenbur_flavor3"));
    public static final SoundEvent EXGLUTENBUR_HIT = SoundEvent.of(id("exglutenbur_hit"));
    public static final SoundEvent ORGA_SHOT = SoundEvent.of(id("orga_shot"));
    public static final SoundEvent ORGA_FLOWER = SoundEvent.of(id("orga_flower"));
    public static final SoundEvent ORGA_REQUIEM_1 = SoundEvent.of(id("orga_requiem_1"));
    public static final SoundEvent ORGA_REQUIEM_2 = SoundEvent.of(id("orga_requiem_2"));
    public static final SoundEvent ORGA_REQUIEM_3 = SoundEvent.of(id("orga_requiem_3"));
    public static final SoundEvent ORGA_REQUIEM_GOLD = SoundEvent.of(id("orga_requiem_gold"));
    public static final SoundEvent ORGA_REQUIEM_PROTECT = SoundEvent.of(id("orga_requiem_protect"));
    public static final SoundEvent ORGA_REQUIEM_HIT = SoundEvent.of(id("orga_requiem_hit"));
    public static final SoundEvent ORGA_RIDER = SoundEvent.of(id("orga_rider"));
    public static final SoundEvent THE_WORLD = SoundEvent.of(id("the_world"));
    public static final SoundEvent THE_WORLD_STAND_UP = SoundEvent.of(id("the_world_stand_up"));
    public static final SoundEvent THE_WORLD_1 = SoundEvent.of(id("the_world_1"));
    public static final SoundEvent THE_WORLD_2 = SoundEvent.of(id("the_world_2"));
    public static final SoundEvent THE_WORLD_3 = SoundEvent.of(id("the_world_3"));
    public static final SoundEvent THE_WORLD_RE = SoundEvent.of(id("the_world_re"));
    public static final SoundEvent ROAD_ROLLER = SoundEvent.of(id("road_roller"));
    public static final SoundEvent DIO_FLAG = SoundEvent.of(id("dio_flag"));
    public static final SoundEvent DIO_HIT = SoundEvent.of(id("dio_hit"));
    public static final SoundEvent NOISE_FURNACE = SoundEvent.of(id("noise_furnace"));
    public static final SoundEvent STAND_THE_WORLD_HIT_1 = SoundEvent.of(id("stand_the_world_hit_1"));
    public static final SoundEvent STAND_THE_WORLD_HIT_2 = SoundEvent.of(id("stand_the_world_hit_2"));
    public static final SoundEvent STAND_STAR_PLATINUM_1 = SoundEvent.of(id("stand_star_platinum_hit_1"));
    public static final SoundEvent STAND_STAR_PLATINUM_2 = SoundEvent.of(id("stand_star_platinum_hit_2"));
    public static final SoundEvent STAND_STAR_PLATINUM_3 = SoundEvent.of(id("stand_star_platinum_hit_3"));
    public static final SoundEvent STAND_STAR_PLATINUM_4 = SoundEvent.of(id("stand_star_platinum_hit_4"));
    public static final SoundEvent STAR_PLATINUM_THE_WORLD_1 = SoundEvent.of(id("star_platinum_the_world_1"));
    public static final SoundEvent STAR_PLATINUM_STAND_UP = SoundEvent.of(id("star_platinum_stand_up"));
    public static final SoundEvent STAR_PLATINUM_THE_WORLD_2 = SoundEvent.of(id("star_platinum_the_world_2"));
    public static final SoundEvent STAR_PLATINUM_THE_WORLD_RE = SoundEvent.of(id("star_platinum_the_world_re"));
    public static final SoundEvent STAND_STAR_PLATINUM_REPEAT_1 = SoundEvent.of(id("stand_star_platinum_repeat_1"));
    public static final SoundEvent STAND_HIEROPHANT_GREEN_SHOOT_1 = SoundEvent.of(id("stand_hierophant_green_shoot_1"));
    public static final SoundEvent STAND_HIEROPHANT_GREEN_SHOOT_2 = SoundEvent.of(id("stand_hierophant_green_shoot_2"));
    public static final SoundEvent STAND_HIEROPHANT_GREEN_EMERALD_SPLASH = SoundEvent.of(id("stand_hierophant_green_emerald_splash"));
    public static final SoundEvent STAND_HIEROPHANT_GREEN_STAND_UP = SoundEvent.of(id("hierophant_green_stand_up"));
    public static final SoundEvent STAND_KILLER_QUEEN_TRIGGER = SoundEvent.of(id("stand_killer_queen_trigger"));
    public static final SoundEvent STAND_KILLER_QUEEN_SHOW_1 = SoundEvent.of(id("stand_killer_queen_show_1"));
    public static final SoundEvent STAND_KILLER_QUEEN_SHOW_2 = SoundEvent.of(id("stand_killer_queen_show_2"));
    public static final SoundEvent STAND_CRAZY_DIAMOND_1 = SoundEvent.of(id("stand_crazy_diamond_1"));
    public static final SoundEvent STAND_CRAZY_DIAMOND_2 = SoundEvent.of(id("stand_crazy_diamond_2"));
    public static final SoundEvent STAND_CRAZY_DIAMOND_3 = SoundEvent.of(id("stand_crazy_diamond_3"));
    public static final SoundEvent STAND_CRAZY_DIAMOND_4 = SoundEvent.of(id("stand_crazy_diamond_4"));
    public static final SoundEvent STAND_CRAZY_DIAMOND_REPAIR_1 = SoundEvent.of(id("stand_crazy_diamond_repair_1"));
    public static final SoundEvent STAND_CRAZY_DIAMOND_REPAIR_2 = SoundEvent.of(id("stand_crazy_diamond_repair_2"));
    public static final SoundEvent STAND_CRAZY_DIAMOND_STAND_UP = SoundEvent.of(id("crazy_diamond_stand_up"));
    public static final SoundEvent STAND_HERMIT_PURPLE_1 = SoundEvent.of(id("stand_hermit_purple_1"));
    public static final SoundEvent STAND_HERMIT_PURPLE_2 = SoundEvent.of(id("stand_hermit_purple_2"));
    public static final SoundEvent STAND_HERMIT_PURPLE_WAVE = SoundEvent.of(id("stand_hermit_purple_wave"));
    public static final SoundEvent STAND_HERMIT_PURPLE_CAMERA_BROKEN = SoundEvent.of(id("stand_hermit_purple_camera_broken"));
    public static final SoundEvent STAND_WHITE_SNAKE_1 = SoundEvent.of(id("stand_white_snake_1"));
    public static final SoundEvent STAND_WHITE_SNAKE_2 = SoundEvent.of(id("stand_white_snake_2"));
    public static final SoundEvent STAND_WHITE_SNAKE_3 = SoundEvent.of(id("stand_white_snake_3"));
    public static final SoundEvent STAND_WHITE_SNAKE_HIT_1 = SoundEvent.of(id("stand_white_snake_hit_1"));
    public static final SoundEvent STAND_WHITE_SNAKE_HIT_2 = SoundEvent.of(id("stand_white_snake_hit_2"));
    public static final SoundEvent STAND_WHITE_SNAKE_HIT_3 = SoundEvent.of(id("stand_white_snake_hit_3"));
    public static final SoundEvent STAND_WHITE_SNAKE_MAKE_DISC_1 = SoundEvent.of(id("stand_white_snake_make_disc_1"));
    public static final SoundEvent STAND_WHITE_SNAKE_MAKE_DISC_2 = SoundEvent.of(id("stand_white_snake_make_disc_2"));
    public static final SoundEvent SHEER_HEART_ATTACK = SoundEvent.of(id("sheer_heart_attack_target"));
    public static final SoundEvent REO_CHERRY = SoundEvent.of(id("reo_cherry"));
    public static final SoundEvent WAVE_OVERDRIVE_1 = SoundEvent.of(id("wave_overdrive_1"));
    public static final SoundEvent WAVE_OVERDRIVE_RUN = SoundEvent.of(id("wave_overdrive_run"));

    /**
     * 显式注册入口：必须在注册表冻结前（mod 入口 onInitialize）调用且仅调用一次。
     * 这里才执行 Registry.register，并顺带填充 SOUND_LIST 供 getSoundByIndex/getSound 使用。
     */
    public static void init() {
        register(ENERGY_HIT);
        register(CHARGE);
        register(WAVE1);
        register(STELLA);
        register(EXGLUTENBUR_1);
        register(EXGLUTENBUR_2);
        register(EXGLUTENBUR_3);
        register(EXGLUTENBUR_HIT);
        register(ORGA_SHOT);
        register(ORGA_FLOWER);
        register(ORGA_REQUIEM_1);
        register(ORGA_REQUIEM_2);
        register(ORGA_REQUIEM_3);
        register(ORGA_REQUIEM_GOLD);
        register(ORGA_REQUIEM_PROTECT);
        register(ORGA_REQUIEM_HIT);
        register(ORGA_RIDER);
        register(THE_WORLD);
        register(THE_WORLD_STAND_UP);
        register(THE_WORLD_1);
        register(THE_WORLD_2);
        register(THE_WORLD_3);
        register(THE_WORLD_RE);
        register(ROAD_ROLLER);
        register(DIO_FLAG);
        register(DIO_HIT);
        register(NOISE_FURNACE);
        register(STAND_THE_WORLD_HIT_1);
        register(STAND_THE_WORLD_HIT_2);
        register(STAND_STAR_PLATINUM_1);
        register(STAND_STAR_PLATINUM_2);
        register(STAND_STAR_PLATINUM_3);
        register(STAND_STAR_PLATINUM_4);
        register(STAR_PLATINUM_THE_WORLD_1);
        register(STAR_PLATINUM_STAND_UP);
        register(STAR_PLATINUM_THE_WORLD_2);
        register(STAR_PLATINUM_THE_WORLD_RE);
        register(STAND_STAR_PLATINUM_REPEAT_1);
        register(STAND_HIEROPHANT_GREEN_SHOOT_1);
        register(STAND_HIEROPHANT_GREEN_SHOOT_2);
        register(STAND_HIEROPHANT_GREEN_EMERALD_SPLASH);
        register(STAND_HIEROPHANT_GREEN_STAND_UP);
        register(STAND_KILLER_QUEEN_TRIGGER);
        register(STAND_KILLER_QUEEN_SHOW_1);
        register(STAND_KILLER_QUEEN_SHOW_2);
        register(STAND_CRAZY_DIAMOND_1);
        register(STAND_CRAZY_DIAMOND_2);
        register(STAND_CRAZY_DIAMOND_3);
        register(STAND_CRAZY_DIAMOND_4);
        register(STAND_CRAZY_DIAMOND_REPAIR_1);
        register(STAND_CRAZY_DIAMOND_REPAIR_2);
        register(STAND_CRAZY_DIAMOND_STAND_UP);
        register(STAND_HERMIT_PURPLE_1);
        register(STAND_HERMIT_PURPLE_2);
        register(STAND_HERMIT_PURPLE_WAVE);
        register(STAND_HERMIT_PURPLE_CAMERA_BROKEN);
        register(STAND_WHITE_SNAKE_1);
        register(STAND_WHITE_SNAKE_2);
        register(STAND_WHITE_SNAKE_3);
        register(STAND_WHITE_SNAKE_HIT_1);
        register(STAND_WHITE_SNAKE_HIT_2);
        register(STAND_WHITE_SNAKE_HIT_3);
        register(STAND_WHITE_SNAKE_MAKE_DISC_1);
        register(STAND_WHITE_SNAKE_MAKE_DISC_2);
        register(SHEER_HEART_ATTACK);
        register(REO_CHERRY);
        register(WAVE_OVERDRIVE_1);
        register(WAVE_OVERDRIVE_RUN);
    }

    /**
     * 兼容旧入口约定：注册动作已收敛到 init()，registerAll 仅为无副作用别名。
     */
    public static void registerAll() {
        init();
    }

    private static Identifier id(String name) {
        return new Identifier(HuajiAgeRemastered.MOD_ID, name);
    }

    private static void register(SoundEvent event) {
        Identifier id = event.getId();
        Registry.register(Registries.SOUND_EVENT, id, event);
        SOUND_LIST.add(event);
    }

    public static SoundEvent getSoundByIndex(int index) {
        return SOUND_LIST.get(index);
    }

    public static SoundEvent getSound(String name) {
        for (SoundEvent sound : SOUND_LIST) {
            if (sound.getId().getPath().equals(name)) {
                return sound;
            }
        }
        return null;
    }
}
