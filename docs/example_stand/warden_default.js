// 攻击态（default）：借原版坚守者模型，用 sway.js 摆动骨骼，并做范围攻击
var Helper = Java.type("org.huajiager.stand.helper.StandPowerHelper");

Java.asJSONCompatible({
    stand: "huajiager:warden",
    stateId: "default",
    stateKey: "entity.minecraft.warden",
    modelId: "minecraft:warden",
    hand: true,
    soundRepeat: true,
    stage: 0,
    // 本状态使用的骨骼动画脚本（这里用工程自带的通用摆动脚本）
    animation: ["huajiager:animation/sway.js"],
    update: function (worldWrapper, entityWrapper, dataWrapper) {
        Helper.rangePunchAttack(entityWrapper.getLivingBase(), 60, 8, 2);
    },
    timeOut: function (worldWrapper, entityWrapper, dataWrapper) {
        Helper.potionDefaultOutOfTime(entityWrapper.getLivingBase());
    }
});
