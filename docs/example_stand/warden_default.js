// 测试替身：用自己的 modelId 借原版坚守者的模型与贴图（不需要资源包）
var Helper = Java.type("org.huajiager.stand.helper.StandPowerHelper");

Java.asJSONCompatible({
    stand: "huajiager:warden",
    stateId: "default",
    stateKey: "entity.minecraft.warden",
    modelId: "minecraft:warden",
    hand: true,
    stage: 0,
    update: function (worldWrapper, entityWrapper, dataWrapper) {
    },
    timeOut: function (worldWrapper, entityWrapper, dataWrapper) {
        Helper.potionDefaultOutOfTime(entityWrapper.getLivingBase());
    }
});
