var Helper = Java.type("org.huajiager.stand.helper.StandPowerHelper");

Java.asJSONCompatible({
//  替身ID-推荐格式："[作者/作品id]：替身ID"
    stand:"huajiager:crazy_diamond",
//  替身的状态ID,不写时默认为“default”，必须存在一个id为“default”的state
//  若stateId带有'%custom'的后缀，模型ID不需要给json文件加上后缀
    stateId:"idle",
//  替身状态名称,若只有一个状态，可不写 -推荐格式：“stand.[替身ID].[替身状态]”
    stateKey:"stand.huajiager.crazy_diamond.idle",
//  模型ID
    modelId:"huajiager:crazy_diamond",
//  替身放出时，第一人称是否显示手臂，不写时默认为true
    hand:true,
//  可解锁该状态的替身等级，不写时默认为0
    stage:0,

/**
 * 替身放出时始终执行的方法
 * @param world 当前所处的世界
 * @param entity 替身使者
 */
    update: function (worldWrapper,entityWrapper,dataWrapper) {
        Helper.MPCharge(entityWrapper.getLivingBase(),80);
    },

/**
 * 替身放出超时执行的方法
 * @param world 当前所处的世界
 * @param entity 替身使者
 */
    timeOut: function (worldWrapper,entityWrapper,dataWrapper) {
        //默认超时效果
       Helper.increaseStandTime(entityWrapper.getLivingBase(),100);
       Helper.potionEffectAdd(entityWrapper.getLivingBase(),"minecraft:hunger",100,5);

    },
    /**
     * 替身技能
     * @param world 当前所处的世界
     * @param entity 替身使者
     */
    capability: function (worldWrapper,entityWrapper,dataWrapper) {
        // 获取玩家手上的物品 （玩家，是否是主手）
        var item_main_hand = Helper.getPlayerHoldItem(entityWrapper.getLivingBase(),true);
        // 修复物品（物品）
        Helper.repairItem(item_main_hand);
        //播放音效（玩家，音效ID，响度）——闲置态释放大招：随机修复音（repair_1/2）
        //（原实现播 crazy_diamond_stand_up，与召唤音同源导致听起来就是召唤音，用户反馈不符。        // 现与其他形态大招一致用修复音，带冷却防连按重头再播）
        Helper.playStandSoundWithCooldown(entityWrapper.getLivingBase(),
            "huajiager:stand_crazy_diamond_repair_" + (Math.floor(Math.random()*2)+1), 0.6);
        //添加药水效果（玩家，药水ID，持续时长<tick，约为1/20秒>，等级-1）
        //250 tick（12.5秒）> 攻击/治疗态 200 tick（10秒），对齐原作"闲置释放药水时间更长"
        Helper.potionEffectAdd(entityWrapper.getLivingBase(),"huajiager:potion_huaji_repair",250,0);
    }

});
