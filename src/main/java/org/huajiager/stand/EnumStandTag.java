package org.huajiager.stand;

/**
 * 替身标签常量枚举。
 * 供替身状态机 / 实体逻辑判断特殊状态标签（如 RIDE 骑乘）。
 */
public class EnumStandTag {
    public enum StateTags {
        FLY("fly"),
        UNDEAD("undead"),
        RIDE("ride"),
        BLOCK_MOVE("block_move"),
        ELEMENT_LIGHT("element_light"),
        DISC_DEPRIVE("disc_deprive"),
        SOUND_DIE("sound-die:");

        StateTags(String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return name;
        }
    }

    public enum StandTags {
        ARROW("arrow");

        StandTags(String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return name;
        }
    }
}
