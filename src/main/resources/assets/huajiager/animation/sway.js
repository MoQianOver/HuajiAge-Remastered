// 通用摇摆动画：按骨骼名做正弦摆动（适合借用原版实体模型，或骨骼名匹配的自定义模型）
Java.asJSONCompatible({
    animation: function (player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, modelMap) {
        var head = modelMap.get("head");
        if (head != undefined) {
            head.setRotateAngleX(headPitch * 0.017453292);
            head.setRotateAngleY(netHeadYaw * 0.017453292);
        }

        var leftArm = modelMap.get("left_arm");
        if (leftArm != undefined) {
            leftArm.setRotateAngleX(Math.sin(ageInTicks * 0.15) * 0.35);
            leftArm.setRotateAngleZ(Math.cos(ageInTicks * 0.12) * 0.12);
        }

        var rightArm = modelMap.get("right_arm");
        if (rightArm != undefined) {
            rightArm.setRotateAngleX(Math.sin(ageInTicks * 0.15 + 3.14) * 0.35);
            rightArm.setRotateAngleZ(-Math.cos(ageInTicks * 0.12) * 0.12);
        }

        var leftTendril = modelMap.get("left_tendril");
        if (leftTendril != undefined) {
            leftTendril.setRotateAngleZ(Math.sin(ageInTicks * 0.1) * 0.25);
        }

        var rightTendril = modelMap.get("right_tendril");
        if (rightTendril != undefined) {
            rightTendril.setRotateAngleZ(-Math.sin(ageInTicks * 0.1) * 0.25);
        }

        var leftRibcage = modelMap.get("left_ribcage");
        if (leftRibcage != undefined) {
            leftRibcage.setRotateAngleY(Math.sin(ageInTicks * 0.05) * 0.15);
        }

        var rightRibcage = modelMap.get("right_ribcage");
        if (rightRibcage != undefined) {
            rightRibcage.setRotateAngleY(-Math.sin(ageInTicks * 0.05) * 0.15);
        }
    }
})
