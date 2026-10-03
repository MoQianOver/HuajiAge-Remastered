package org.huajiager.loot;

import java.util.Map;

import org.huajiager.HuajiAgeRemastered;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * 宝箱战利品注册：自定义掉落函数 + 把 4 张自定义表按概率挂进原版宝箱表。
 *
 * <p>挂法与落点：末地城宝箱 → huaji_loot、沙漠神殿 → stand_loot_desert、
 * 丛林神庙 → stand_loot_temple、下界要塞 → stand_loot_nether。命中时新增一个
 * rolls 0~1 的池，池内只有一条指向自定义表的 LootTableEntry（权重 1）。</p>
 */
public final class LootLoader {

    /** 掉落函数类型（JSON 里写作 huajiager:set_nbts_random）。 */
    public static final LootFunctionType SET_NBTS_RANDOM =
            new LootFunctionType(new LootFunctionSetNbtsRandom.Serializer());

    /** 原版宝箱表 → 挂进去的自定义表。 */
    private static final Map<Identifier, Identifier> CHEST_INJECTIONS = Map.of(
            Identifier.of("minecraft", "chests/end_city_treasure"), id("huaji_loot"),
            Identifier.of("minecraft", "chests/desert_pyramid"), id("stand_loot_desert"),
            Identifier.of("minecraft", "chests/jungle_temple"), id("stand_loot_temple"),
            Identifier.of("minecraft", "chests/nether_bridge"), id("stand_loot_nether"));

    private LootLoader() {
    }

    public static void init() {
        Registry.register(Registries.LOOT_FUNCTION_TYPE, id("set_nbts_random"), SET_NBTS_RANDOM);

        LootTableEvents.MODIFY.register((resourceManager, lootManager, tableId, tableBuilder, source) -> {
            Identifier customTable = CHEST_INJECTIONS.get(tableId);
            if (customTable == null) {
                return;
            }
            // 注入是数据包级一次性动作，这里留一行日志便于整合包作者确认是否生效
            HuajiAgeRemastered.LOGGER.info("[HuajiAge] Injected chest loot table {} into {}",
                    customTable, tableId);
            tableBuilder.pool(LootPool.builder()
                    .rolls(UniformLootNumberProvider.create(0.0f, 1.0f))
                    .with(LootTableEntry.builder(customTable)));
        });
    }

    private static Identifier id(String path) {
        return Identifier.of(HuajiAgeRemastered.MOD_ID, path);
    }
}
