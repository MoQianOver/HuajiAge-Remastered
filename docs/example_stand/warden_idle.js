// 待机态（idle）：同一模型，只让头部跟随玩家视线，不再攻击、不再循环音
Java.asJSONCompatible({
    stand: "huajiager:warden",
    stateId: "idle",
    stateKey: "entity.minecraft.warden",
    modelId: "minecraft:warden",
    hand: true,
    soundRepeat: false,
    stage: 0,
    animation: ["huajiager:animation/head.js"],
    update: function (worldWrapper, entityWrapper, dataWrapper) {
    }
});
