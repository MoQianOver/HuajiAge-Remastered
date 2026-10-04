// 女仆替身（仅在安装车万女仆时注册）：属性与技能对齐原版 StandMaid / StateMaidDefault
var Helper = Java.type("org.huajiager.stand.helper.StandPowerHelper");

Java.asJSONCompatible({
    stand: "huajiager:maid",
    stateId: "default",
    stateKey: "stand.huajiager.maid.default",
    modelId: "huajiager:maid_01",
    hand: true,
    soundRepeat: true,
    stage: 0,
    animation: ["huajiager:animation/head.js"],

    // 替身放出期间每 tick 补幸运 5 秒（原版只在快到期时补，这里每 tick 重施等价）
    update: function (worldWrapper, entityWrapper, dataWrapper) {
        Helper.potionEffectAdd(entityWrapper.getLivingBase(), "luck", 100, 1);
    },

    // 技能：速度III + 再生 + 发光，各 300 tick，再随机追加一条增益，
    // 并播原版的两句聊天提示 + 女仆驯服音 + 玩家升级音
    capability: function (worldWrapper, entityWrapper, dataWrapper) {
        var user = entityWrapper.getLivingBase();
        Helper.potionEffectAdd(user, "speed", 300, 3);
        Helper.potionEffectAdd(user, "regeneration", 300, 1);
        Helper.potionEffectAdd(user, "glowing", 300, 1);

        var pool = [
            ["jump_boost", 2],
            ["jump_boost", 3],
            ["saturation", 2],
            ["absorption", 3],
            ["absorption", 4],
            ["health_boost", 2],
            ["resistance", 4]
        ];
        var pick = pool[Math.floor(Math.random() * pool.length)];
        Helper.potionEffectAdd(user, pick[0], 300, pick[1]);

        Helper.sendMessage(user, "§c§l女§f§l仆");
        Helper.sendMessage(user, "\\(>￣▽￣<)/");
        Helper.playStandSoundWithCooldown(user, "touhou_little_maid:maid.ai.tamed", 2.0);
        Helper.playStandSoundWithCooldown(user, "minecraft:entity.player.levelup", 2.0);
        return true;
    },

    // 超时：替身药水 5 秒 + 饥饿II 5 秒
    timeOut: function (worldWrapper, entityWrapper, dataWrapper) {
        Helper.potionDefaultOutOfTime(entityWrapper.getLivingBase());
        Helper.potionEffectAdd(entityWrapper.getLivingBase(), "minecraft:hunger", 100, 2);
    }
});
