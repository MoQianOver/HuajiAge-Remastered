var Helper = Java.type("org.huajiager.stand.helper.StandPowerHelper");

Java.asJSONCompatible({
//  替身ID-推荐格式："[作者/作品id]：替身ID"
    stand:"huajiager:hermit_purple",
//  替身的状态ID,不写时默认为“default”，必须存在一个id为“default”的state
//  若stateId带有'%custom'的后缀，模型ID不需要给json文件加上后缀
    stateId:"overdrive",
//  替身状态名称,若只有一个状态，可不写 -推荐格式：“stand.[替身ID].[替身状态]”
    stateKey:"stand.huajiager.hermit_purple.overdrive",
//  模型ID
    modelId:"huajiager:hermit_purple",
//  一些属性标签，便于使用mod内置的能力
    stateTags:["element_light"],
//  替身放出时，第一人称是否显示手臂，不写时默认为true
    hand:true,
//  替身放出时，是否有重复音乐播放
    soundRepeat:false,
//  可解锁该状态的替身等级，不写时默认为0
    stage:1,

/**
 * 替身放出时始终执行的方法
 * @param world 当前所处的世界
 * @param entity 替身使者
 */
    update: function (worldWrapper,entityWrapper,dataWrapper) {
    var level = dataWrapper.getStage();
         // 固定时长刷新（600tick=30秒），与 addStatusEffect 一致不累加。         // 替身收回时 MessageStandUp 会移除这些效果
         Helper.potionEffectAdd(entityWrapper.getLivingBase(),"huajiager:potion_huaji_overdrive",600,1);
         Helper.potionEffectAdd(entityWrapper.getLivingBase(),"regeneration",600,3);
         Helper.potionEffectAdd(entityWrapper.getLivingBase(),"speed",600,3+level);
         Helper.potionEffectAdd(entityWrapper.getLivingBase(),"strength",600,3+level);
         Helper.potionEffectAdd(entityWrapper.getLivingBase(),"jump_boost",600,3+level);
         // 波纹疾走蓄力标记：每 tick 维持 buffer=200 / buffTag=overdrive，
         // 使 ItemWaveKnife.refreshWaveByOverdrive 联动判据持续满足（切换模式即生效，
         // 重进世界后仍处于 overdrive 态也会自动恢复）；切回 default 时由
         // MessageStandModeSwitch 清除，避免 buffTag 残留导致 isHermitOverdrive 误判。
         dataWrapper.setBuffer(200);
         dataWrapper.setBufferTag("huajiager.buff.overdrive");
    },

/**
 * 替身放出超时执行的方法
 * @param world 当前所处的世界
 * @param entity 替身使者
 */
    timeOut: function (worldWrapper,entityWrapper,dataWrapper) {
       Helper.increaseStandTime(entityWrapper.getLivingBase(),200);
       Helper.potionEffectAdd(entityWrapper.getLivingBase(),"minecraft:hunger",200,10);
       Helper.potionEffectAdd(entityWrapper.getLivingBase(),"minecraft:slowness",200,2);

    },
    /**
     * 替身技能
     * @param world 当前所处的世界
     * @param entity 替身使者
     */
    capability: function (worldWrapper,entityWrapper,dataWrapper) {
        Helper.sendMessage(entityWrapper.getLivingBase(),"stand.huajiager.skill.huajiager.hermit_purple.run");
        Helper.playSound(entityWrapper.getLivingBase(), "huajiager:wave_overdrive_run", 6, 1);
        Helper.removeBadPotion(entityWrapper.getLivingBase());
        Helper.potionEffectAdd(entityWrapper.getLivingBase(),"minecraft:speed",200,10);
        Helper.potionEffectAdd(entityWrapper.getLivingBase(),"minecraft:jump_boost",200,10);
        Helper.addItemToplayer(entityWrapper.getLivingBase(),"minecraft:ender_pearl",2);

        dataWrapper.setBuffer(200);
        dataWrapper.setBufferTag("huajiager.buff.overdrive");

    }

});
