package org.huajiager.client.render.model;

import org.huajiager.stand.entity.EntityStandBase;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * 疯狂钻石（CRAZY DIAMOND）模型。
 *
 * 数值照搬 Blockbench JSON（models/entity/crazy_diamond.json，128x128 UV）。
 * 由 gen_crazy_diamond.py 程序化生成：
 * 1) setRotationPoint 已转为相对父节点局部坐标（Blockbench Y-up -> MC Y-down，
 *    翻转坐标差：childFliped - parentFliped），并完整恢复 JSON parent 挂载链，
 *    修复此前 119 个骨骼全部为孤儿节点导致的"只见头与半个身子、手脚缺失"。 * 2) addBox 为相对 bone pivot 的局部坐标。 * 3) viewFirst 子树（第一人称专用手部）保留字段但不挂载、不在第三人称渲染。
 * 配合 RenderStandBase 矩阵（180-yaw + scale(-1,-1,1)）渲染即为正立悬浮造型。
 */
public class ModelCrazyDiamond extends HAModelBase implements StandAnimatedModel {

	private final HAModelPart head;
	private final HAModelPart Shape1;
	private final HAModelPart Shape6;
	private final HAModelPart Shape5;
	private final HAModelPart Shape2;
	private final HAModelPart Shape46;
	private final HAModelPart Shape7;
	private final HAModelPart Shape8;
	private final HAModelPart glass;
	private final HAModelPart Shape3;
	private final HAModelPart bone;
	private final HAModelPart Shape15;
	private final HAModelPart Shape17;
	private final HAModelPart Shape18;
	private final HAModelPart Shape16;
	private final HAModelPart Shape4;
	private final HAModelPart heart7;
	private final HAModelPart Shape60;
	private final HAModelPart Shape61;
	private final HAModelPart body;
	private final HAModelPart bodydown;
	private final HAModelPart Shape10;
	private final HAModelPart Shape19;
	private final HAModelPart Shape20;
	private final HAModelPart Shape21;
	private final HAModelPart Shape22;
	private final HAModelPart heart;
	private final HAModelPart Shape11;
	private final HAModelPart Shape23;
	private final HAModelPart heart2;
	private final HAModelPart Shape24;
	private final HAModelPart Shape25;
	private final HAModelPart Shape12;
	private final HAModelPart Shape53;
	private final HAModelPart Shape54;
	private final HAModelPart Shape55;
	private final HAModelPart Shape56;
	private final HAModelPart Shape57;
	private final HAModelPart Shape58;
	private final HAModelPart Shape29;
	private final HAModelPart Shape34;
	private final HAModelPart Shape35;
	private final HAModelPart Shape36;
	private final HAModelPart Shape37;
	private final HAModelPart Shape38;
	private final HAModelPart Shape49;
	private final HAModelPart Shape50;
	private final HAModelPart Shape51;
	private final HAModelPart Shape52;
	private final HAModelPart Shape39;
	private final HAModelPart Shape40;
	private final HAModelPart Shape41;
	private final HAModelPart Shape42;
	private final HAModelPart Shape47;
	private final HAModelPart Shape48;
	private final HAModelPart Shape43;
	private final HAModelPart Shape44;
	private final HAModelPart leftarm;
	private final HAModelPart rightarm;
	private final HAModelPart leftleg;
	private final HAModelPart heart3;
	private final HAModelPart Shape26;
	private final HAModelPart Shape27;
	private final HAModelPart legdownl;
	private final HAModelPart rightleg;
	private final HAModelPart heart4;
	private final HAModelPart Shape9;
	private final HAModelPart Shape28;
	private final HAModelPart legdownr;
	private final HAModelPart viewFirst;
	private final HAModelPart redHalo;
	private final HAModelPart left_hands_ro;
	private final HAModelPart handl1;
	private final HAModelPart armmiddle;
	private final HAModelPart heart5;
	private final HAModelPart Shape30;
	private final HAModelPart Shape31;
	private final HAModelPart handl2;
	private final HAModelPart armmiddle3;
	private final HAModelPart heart8;
	private final HAModelPart Shape13;
	private final HAModelPart Shape14;
	private final HAModelPart handl3;
	private final HAModelPart armmiddle4;
	private final HAModelPart heart9;
	private final HAModelPart Shape45;
	private final HAModelPart Shape59;
	private final HAModelPart handl4;
	private final HAModelPart armmiddle5;
	private final HAModelPart heart10;
	private final HAModelPart Shape62;
	private final HAModelPart Shape63;
	private final HAModelPart handl5;
	private final HAModelPart armmiddle6;
	private final HAModelPart heart11;
	private final HAModelPart Shape64;
	private final HAModelPart Shape65;
	private final HAModelPart right_hands_ro;
	private final HAModelPart handr1;
	private final HAModelPart armmiddle7;
	private final HAModelPart heart12;
	private final HAModelPart Shape66;
	private final HAModelPart Shape67;
	private final HAModelPart handr2;
	private final HAModelPart armmiddle8;
	private final HAModelPart heart13;
	private final HAModelPart Shape68;
	private final HAModelPart Shape69;
	private final HAModelPart handr3;
	private final HAModelPart armmiddle9;
	private final HAModelPart heart14;
	private final HAModelPart Shape70;
	private final HAModelPart Shape71;
	private final HAModelPart handr4;
	private final HAModelPart armmiddle10;
	private final HAModelPart heart15;
	private final HAModelPart Shape72;
	private final HAModelPart Shape73;
	private final HAModelPart handr5;
	private final HAModelPart armmiddle11;
	private final HAModelPart heart16;
	private final HAModelPart Shape74;
	private final HAModelPart Shape75;

	private final HAModelPart root;

public ModelCrazyDiamond() {
	super(128, 128);
	this.root = new HAModelPart(this);

	head = new HAModelPart(this);
	head.setRotationPoint(0F, 0F, 0F);
	setRotation(head, 12.5F, 0F, 0F);

	Shape1 = new HAModelPart(this);
	Shape1.setRotationPoint(0F, 0F, 0F);
	setRotation(Shape1, 22.99853F, 0F, 0F);

	Shape6 = new HAModelPart(this);
	Shape6.setRotationPoint(3.854F, -7.87219F, 2.59755F);
	setRotation(Shape6, 27.99853F, 37.5F, 20F);

	Shape5 = new HAModelPart(this);
	Shape5.setRotationPoint(-3.854F, -7.87219F, 2.59755F);
	setRotation(Shape5, 27.99853F, -37.5F, -20F);

	Shape2 = new HAModelPart(this);
	Shape2.setRotationPoint(0F, 0F, 0F);
	setRotation(Shape2, 3.0023F, 0F, 0F);

	Shape46 = new HAModelPart(this);
	Shape46.setRotationPoint(0F, 1.4F, 0F);
	setRotation(Shape46, 3.0023F, 0F, 0F);

	Shape7 = new HAModelPart(this);
	Shape7.setRotationPoint(0.3F, 2.94316F, -0.74687F);
	setRotation(Shape7, 3.0023F, 0F, 0F);

	Shape8 = new HAModelPart(this);
	Shape8.setRotationPoint(-0.3F, 2.94316F, -0.74687F);
	setRotation(Shape8, 3.0023F, 0F, 0F);

	glass = new HAModelPart(this);
	glass.setRotationPoint(0F, -0.8F, 0.1F);
	setRotation(glass, 11F, 0F, 0F);

	Shape3 = new HAModelPart(this);
	Shape3.setRotationPoint(0F, 0F, 1F);
	setRotation(Shape3, 0F, 63.00244F, 0F);

	bone = new HAModelPart(this);
	bone.setRotationPoint(1.51113F, -4.22513F, -2.5709F);

	Shape15 = new HAModelPart(this);
	Shape15.setRotationPoint(0F, 0F, 0F);
	setRotation(Shape15, 0F, 65.00244F, 0F);

	Shape17 = new HAModelPart(this);
	Shape17.setRotationPoint(0.0606F, -0.11066F, 0.56928F);
	setRotation(Shape17, 0F, 65.00244F, 0F);

	Shape18 = new HAModelPart(this);
	Shape18.setRotationPoint(-3.08286F, -0.11066F, 0.56928F);
	setRotation(Shape18, 0F, -65.00244F, 0F);

	Shape16 = new HAModelPart(this);
	Shape16.setRotationPoint(-3.29415F, -0.02419F, 0.12445F);
	setRotation(Shape16, 0F, -65.00244F, 0F);

	Shape4 = new HAModelPart(this);
	Shape4.setRotationPoint(0F, 0F, 1F);
	setRotation(Shape4, 0F, -63.00244F, 0F);

	heart7 = new HAModelPart(this);
	heart7.setRotationPoint(-2.55F, 5.5F, -0.8F);

	Shape60 = new HAModelPart(this);
	Shape60.setRotationPoint(2.17174F, -5.33824F, -2.2F);
	setRotation(Shape60, 0F, 0F, 44F);

	Shape61 = new HAModelPart(this);
	Shape61.setRotationPoint(2.91106F, -5.3333F, -2.2F);
	setRotation(Shape61, 0F, 0F, -44F);

	body = new HAModelPart(this);
	body.setRotationPoint(0F, 0F, 0F);
	setRotation(body, 7.5F, 0F, 0F);

	bodydown = new HAModelPart(this);
	bodydown.setRotationPoint(0F, 0F, 0F);

	Shape10 = new HAModelPart(this);
	Shape10.setRotationPoint(0F, 0F, 0F);

	Shape19 = new HAModelPart(this);
	Shape19.setRotationPoint(0F, 0F, 6.6F);

	Shape20 = new HAModelPart(this);
	Shape20.setRotationPoint(0F, 5F, 6.6F);

	Shape21 = new HAModelPart(this);
	Shape21.setRotationPoint(-1.69096F, 3.03006F, 6.5F);
	setRotation(Shape21, 0F, 0F, 65F);

	Shape22 = new HAModelPart(this);
	Shape22.setRotationPoint(1.69096F, 3.03006F, 6.5F);
	setRotation(Shape22, 0F, 0F, -65F);

	heart = new HAModelPart(this);
	heart.setRotationPoint(-0.6F, 6.1F, -2F);

	Shape11 = new HAModelPart(this);
	Shape11.setRotationPoint(0F, 0F, 0F);
	setRotation(Shape11, 0F, 0F, 40F);

	Shape23 = new HAModelPart(this);
	Shape23.setRotationPoint(1.2F, 0F, 0F);
	setRotation(Shape23, 0F, 0F, -40F);

	heart2 = new HAModelPart(this);
	heart2.setRotationPoint(-0.6F, 9.9F, -2.3F);

	Shape24 = new HAModelPart(this);
	Shape24.setRotationPoint(0F, 0F, 0F);
	setRotation(Shape24, 0F, 0F, 40F);

	Shape25 = new HAModelPart(this);
	Shape25.setRotationPoint(1.2F, 0F, 0F);
	setRotation(Shape25, 0F, 0F, -40F);

	Shape12 = new HAModelPart(this);
	Shape12.setRotationPoint(0F, 0F, 0.3F);

	Shape53 = new HAModelPart(this);
	Shape53.setRotationPoint(-6.55F, 20.23333F, 0.45F);
	setRotation(Shape53, 0F, 19F, 14F);

	Shape54 = new HAModelPart(this);
	Shape54.setRotationPoint(-4.63775F, -18.199F, 3.45299F);
	setRotation(Shape54, 30F, -164F, 4.5F);

	Shape55 = new HAModelPart(this);
	Shape55.setRotationPoint(-4.86788F, -19.08627F, 2.28877F);
	setRotation(Shape55, 30F, -131F, 4.5F);

	Shape56 = new HAModelPart(this);
	Shape56.setRotationPoint(-5.94959F, -20.89825F, 0.10613F);
	setRotation(Shape56, 30F, 156.5F, 4.5F);

	Shape57 = new HAModelPart(this);
	Shape57.setRotationPoint(-4.91214F, -20.99093F, -1.83426F);
	setRotation(Shape57, -30F, 120.5F, 4.5F);

	Shape58 = new HAModelPart(this);
	Shape58.setRotationPoint(-3.29716F, -19.39259F, -2.14978F);
	setRotation(Shape58, -30F, 67.5F, 4.5F);

	Shape29 = new HAModelPart(this);
	Shape29.setRotationPoint(6.55F, 20.23333F, 0.45F);
	setRotation(Shape29, 0F, -19F, -14F);

	Shape34 = new HAModelPart(this);
	Shape34.setRotationPoint(4.63775F, -18.199F, 3.45299F);
	setRotation(Shape34, 30F, 164F, -4.5F);

	Shape35 = new HAModelPart(this);
	Shape35.setRotationPoint(4.86788F, -19.08627F, 2.28877F);
	setRotation(Shape35, 30F, 131F, -4.5F);

	Shape36 = new HAModelPart(this);
	Shape36.setRotationPoint(5.94959F, -20.89825F, 0.10613F);
	setRotation(Shape36, 30F, -156.5F, -4.5F);

	Shape37 = new HAModelPart(this);
	Shape37.setRotationPoint(4.91214F, -20.99093F, -1.83426F);
	setRotation(Shape37, -30F, -120.5F, -4.5F);

	Shape38 = new HAModelPart(this);
	Shape38.setRotationPoint(3.29716F, -19.39259F, -2.14978F);
	setRotation(Shape38, -30F, -67.5F, -4.5F);

	Shape49 = new HAModelPart(this);
	Shape49.setRotationPoint(-8.675F, 23.625F, -0.7F);
	setRotation(Shape49, 0F, 6F, 44F);

	Shape50 = new HAModelPart(this);
	Shape50.setRotationPoint(-15.29361F, -20.68466F, 2.05746F);
	setRotation(Shape50, 0F, -166F, 27.5F);

	Shape51 = new HAModelPart(this);
	Shape51.setRotationPoint(-15.43015F, -20.89931F, -0.33137F);
	setRotation(Shape51, 7.5F, 162F, 29.5F);

	Shape52 = new HAModelPart(this);
	Shape52.setRotationPoint(-12.23378F, -19.69533F, -3.06898F);
	setRotation(Shape52, 7.5F, 69F, 29.5F);

	Shape39 = new HAModelPart(this);
	Shape39.setRotationPoint(8.675F, 23.625F, -0.7F);
	setRotation(Shape39, 0F, -6F, -44F);

	Shape40 = new HAModelPart(this);
	Shape40.setRotationPoint(15.29361F, -20.68466F, 2.05746F);
	setRotation(Shape40, 0F, 166F, -27.5F);

	Shape41 = new HAModelPart(this);
	Shape41.setRotationPoint(15.43015F, -20.89931F, -0.33137F);
	setRotation(Shape41, 7.5F, -162F, -29.5F);

	Shape42 = new HAModelPart(this);
	Shape42.setRotationPoint(12.23378F, -19.69533F, -3.06898F);
	setRotation(Shape42, 7.5F, -69F, -29.5F);

	Shape47 = new HAModelPart(this);
	Shape47.setRotationPoint(7.85F, -8.8F, 6.7F);
	setRotation(Shape47, -172.62358F, 20.51978F, 64.33982F);

	Shape48 = new HAModelPart(this);
	Shape48.setRotationPoint(3.45822F, -11.64008F, 2.44865F);
	setRotation(Shape48, -136.62358F, 25.51978F, 161.33982F);

	Shape43 = new HAModelPart(this);
	Shape43.setRotationPoint(-7.85F, -8.8F, 6.7F);
	setRotation(Shape43, -172.62358F, -20.51978F, -64.33982F);

	Shape44 = new HAModelPart(this);
	Shape44.setRotationPoint(-3.45822F, -11.64008F, 2.44865F);
	setRotation(Shape44, -136.62358F, -25.51978F, -161.33982F);

	leftarm = new HAModelPart(this);
	leftarm.setRotationPoint(5F, 2F, -0.1F);
	setRotation(leftarm, 20F, 0F, -10F);

	rightarm = new HAModelPart(this);
	rightarm.setRotationPoint(-5F, 2F, -0.1F);
	setRotation(rightarm, 12.08681F, -3.21132F, 14.65993F);

	leftleg = new HAModelPart(this);
	leftleg.setRotationPoint(2F, 12F, 1F);
	setRotation(leftleg, -32.00997F, -11.17442F, -16.41349F);

	heart3 = new HAModelPart(this);
	heart3.setRotationPoint(-2F, 12F, -1F);

	Shape26 = new HAModelPart(this);
	Shape26.setRotationPoint(-0.6F, -12.9F, -2.5F);
	setRotation(Shape26, 0F, 0F, 40F);

	Shape27 = new HAModelPart(this);
	Shape27.setRotationPoint(0.6F, -12.9F, -2.5F);
	setRotation(Shape27, 0F, 0F, -40F);

	legdownl = new HAModelPart(this);
	legdownl.setRotationPoint(1.26183F, 5.8899F, 1.14531F);
	setRotation(legdownl, 42.5F, 0F, 0F);

	rightleg = new HAModelPart(this);
	rightleg.setRotationPoint(-2F, 12F, 1F);
	setRotation(rightleg, -28.48604F, 7.26717F, 17.629F);

	heart4 = new HAModelPart(this);
	heart4.setRotationPoint(2F, 12F, -1F);

	Shape9 = new HAModelPart(this);
	Shape9.setRotationPoint(0.6F, -12.9F, -2.5F);
	setRotation(Shape9, 0F, 0F, -40F);

	Shape28 = new HAModelPart(this);
	Shape28.setRotationPoint(-0.6F, -12.9F, -2.5F);
	setRotation(Shape28, 0F, 0F, 40F);

	legdownr = new HAModelPart(this);
	legdownr.setRotationPoint(-0.2311F, 7.37158F, -0.11091F);
	setRotation(legdownr, 42.5F, -7.5F, -5F);

	viewFirst = new HAModelPart(this);
	viewFirst.setRotationPoint(0F, 3.45F, 0.2F);

	// 治疗态粉红罩：直接用 crazy_diamond_heal.png 左下角粉红区域
	// (0,80)-(24,96) 作 UV，6x6x6 盒六面均落在粉红区内；不改贴图像素。
	redHalo = new HAModelPart(this);
	redHalo.setRotationPoint(0F, 0F, 0F);
	redHalo.setTextureOffset(0, 80);
	redHalo.addBox(-3, -3, -3, 6, 6, 6, 0.0F);

	left_hands_ro = new HAModelPart(this);
	left_hands_ro.setRotationPoint(0F, 0F, 0F);

	handl1 = new HAModelPart(this);
	handl1.setRotationPoint(0F, 0F, 0F);
	setRotation(handl1, -90F, 0F, 0F);

	armmiddle = new HAModelPart(this);
	armmiddle.setRotationPoint(0F, 0.55F, -0.2F);

	heart5 = new HAModelPart(this);
	heart5.setRotationPoint(10.7F, -5.6F, -5F);
	setRotation(heart5, 0F, -90F, 0F);

	Shape30 = new HAModelPart(this);
	Shape30.setRotationPoint(-0.6F, -12.9F, -2.5F);
	setRotation(Shape30, 0F, 0F, 40F);

	Shape31 = new HAModelPart(this);
	Shape31.setRotationPoint(0.6F, -12.9F, -2.5F);
	setRotation(Shape31, 0F, 0F, -40F);

	handl2 = new HAModelPart(this);
	handl2.setRotationPoint(0F, 0F, 0F);
	setRotation(handl2, -90F, 0F, 0F);

	armmiddle3 = new HAModelPart(this);
	armmiddle3.setRotationPoint(0F, 0.55F, -0.2F);

	heart8 = new HAModelPart(this);
	heart8.setRotationPoint(10.7F, -5.6F, -5F);
	setRotation(heart8, 0F, -90F, 0F);

	Shape13 = new HAModelPart(this);
	Shape13.setRotationPoint(-0.6F, -12.9F, -2.5F);
	setRotation(Shape13, 0F, 0F, 40F);

	Shape14 = new HAModelPart(this);
	Shape14.setRotationPoint(0.6F, -12.9F, -2.5F);
	setRotation(Shape14, 0F, 0F, -40F);

	handl3 = new HAModelPart(this);
	handl3.setRotationPoint(0F, 0F, 0F);
	setRotation(handl3, -90F, 0F, 0F);

	armmiddle4 = new HAModelPart(this);
	armmiddle4.setRotationPoint(0F, 0.55F, -0.2F);

	heart9 = new HAModelPart(this);
	heart9.setRotationPoint(10.7F, -5.6F, -5F);
	setRotation(heart9, 0F, -90F, 0F);

	Shape45 = new HAModelPart(this);
	Shape45.setRotationPoint(-0.6F, -12.9F, -2.5F);
	setRotation(Shape45, 0F, 0F, 40F);

	Shape59 = new HAModelPart(this);
	Shape59.setRotationPoint(0.6F, -12.9F, -2.5F);
	setRotation(Shape59, 0F, 0F, -40F);

	handl4 = new HAModelPart(this);
	handl4.setRotationPoint(0F, 0F, 0F);
	setRotation(handl4, -90F, 0F, 0F);

	armmiddle5 = new HAModelPart(this);
	armmiddle5.setRotationPoint(0F, 0.55F, -0.2F);

	heart10 = new HAModelPart(this);
	heart10.setRotationPoint(10.7F, -5.6F, -5F);
	setRotation(heart10, 0F, -90F, 0F);

	Shape62 = new HAModelPart(this);
	Shape62.setRotationPoint(-0.6F, -12.9F, -2.5F);
	setRotation(Shape62, 0F, 0F, 40F);

	Shape63 = new HAModelPart(this);
	Shape63.setRotationPoint(0.6F, -12.9F, -2.5F);
	setRotation(Shape63, 0F, 0F, -40F);

	handl5 = new HAModelPart(this);
	handl5.setRotationPoint(0F, 0F, 0F);
	setRotation(handl5, -90F, 0F, 0F);

	armmiddle6 = new HAModelPart(this);
	armmiddle6.setRotationPoint(0F, 0.55F, -0.2F);

	heart11 = new HAModelPart(this);
	heart11.setRotationPoint(10.7F, -5.6F, -5F);
	setRotation(heart11, 0F, -90F, 0F);

	Shape64 = new HAModelPart(this);
	Shape64.setRotationPoint(-0.6F, -12.9F, -2.5F);
	setRotation(Shape64, 0F, 0F, 40F);

	Shape65 = new HAModelPart(this);
	Shape65.setRotationPoint(0.6F, -12.9F, -2.5F);
	setRotation(Shape65, 0F, 0F, -40F);

	right_hands_ro = new HAModelPart(this);
	right_hands_ro.setRotationPoint(0F, 0F, 0F);

	handr1 = new HAModelPart(this);
	handr1.setRotationPoint(0F, 0F, 0F);
	setRotation(handr1, -90F, 0F, 0F);

	armmiddle7 = new HAModelPart(this);
	armmiddle7.setRotationPoint(0F, 0.55F, -0.2F);

	heart12 = new HAModelPart(this);
	heart12.setRotationPoint(-10.7F, -5.6F, -5F);
	setRotation(heart12, 0F, 90F, 0F);

	Shape66 = new HAModelPart(this);
	Shape66.setRotationPoint(0.6F, -12.9F, -2.5F);
	setRotation(Shape66, 0F, 0F, -40F);

	Shape67 = new HAModelPart(this);
	Shape67.setRotationPoint(-0.6F, -12.9F, -2.5F);
	setRotation(Shape67, 0F, 0F, 40F);

	handr2 = new HAModelPart(this);
	handr2.setRotationPoint(0F, 0F, 0F);
	setRotation(handr2, -90F, 0F, 0F);

	armmiddle8 = new HAModelPart(this);
	armmiddle8.setRotationPoint(0F, 0.55F, -0.2F);

	heart13 = new HAModelPart(this);
	heart13.setRotationPoint(-10.7F, -5.6F, -5F);
	setRotation(heart13, 0F, 90F, 0F);

	Shape68 = new HAModelPart(this);
	Shape68.setRotationPoint(0.6F, -12.9F, -2.5F);
	setRotation(Shape68, 0F, 0F, -40F);

	Shape69 = new HAModelPart(this);
	Shape69.setRotationPoint(-0.6F, -12.9F, -2.5F);
	setRotation(Shape69, 0F, 0F, 40F);

	handr3 = new HAModelPart(this);
	handr3.setRotationPoint(0F, 0F, 0F);
	setRotation(handr3, -90F, 0F, 0F);

	armmiddle9 = new HAModelPart(this);
	armmiddle9.setRotationPoint(0F, 0.55F, -0.2F);

	heart14 = new HAModelPart(this);
	heart14.setRotationPoint(-10.7F, -5.6F, -5F);
	setRotation(heart14, 0F, 90F, 0F);

	Shape70 = new HAModelPart(this);
	Shape70.setRotationPoint(0.6F, -12.9F, -2.5F);
	setRotation(Shape70, 0F, 0F, -40F);

	Shape71 = new HAModelPart(this);
	Shape71.setRotationPoint(-0.6F, -12.9F, -2.5F);
	setRotation(Shape71, 0F, 0F, 40F);

	handr4 = new HAModelPart(this);
	handr4.setRotationPoint(0F, 0F, 0F);
	setRotation(handr4, -90F, 0F, 0F);

	armmiddle10 = new HAModelPart(this);
	armmiddle10.setRotationPoint(0F, 0.55F, -0.2F);

	heart15 = new HAModelPart(this);
	heart15.setRotationPoint(-10.7F, -5.6F, -5F);
	setRotation(heart15, 0F, 90F, 0F);

	Shape72 = new HAModelPart(this);
	Shape72.setRotationPoint(0.6F, -12.9F, -2.5F);
	setRotation(Shape72, 0F, 0F, -40F);

	Shape73 = new HAModelPart(this);
	Shape73.setRotationPoint(-0.6F, -12.9F, -2.5F);
	setRotation(Shape73, 0F, 0F, 40F);

	handr5 = new HAModelPart(this);
	handr5.setRotationPoint(0F, 0F, 0F);
	setRotation(handr5, -90F, 0F, 0F);

	armmiddle11 = new HAModelPart(this);
	armmiddle11.setRotationPoint(0F, 0.55F, -0.2F);

	heart16 = new HAModelPart(this);
	heart16.setRotationPoint(-10.7F, -5.6F, -5F);
	setRotation(heart16, 0F, 90F, 0F);

	Shape74 = new HAModelPart(this);
	Shape74.setRotationPoint(0.6F, -12.9F, -2.5F);
	setRotation(Shape74, 0F, 0F, -40F);

	Shape75 = new HAModelPart(this);
	Shape75.setRotationPoint(-0.6F, -12.9F, -2.5F);
	setRotation(Shape75, 0F, 0F, 40F);

	head.addBox(21, 21, -3.5F, -6F, -3F, 7F, 6F, 7F, 0F, false);
	Shape1.addBox(26, 5, -4.6F, -8F, 0F, 9.1F, 3F, 6F, 0.01F, false);
	Shape6.addBox(65, 68, -3F, -1.5F, -2F, 3F, 3F, 3F, 0.02F, false);
	Shape5.addBox(0, 68, 0F, -1.5F, -2F, 3F, 3F, 3F, 0.02F, false);
	Shape2.addBox(0, 0, -4.5F, -7.2F, -1.7F, 9F, 4F, 7F, 0F, false);
	Shape46.addBox(22, 14, -4.5F, -4.2F, 3.3F, 9F, 1F, 2F, 0F, false);
	Shape7.addBox(62, 17, -4.5F, -6.2F, -1.7F, 1F, 3F, 7F, 0F, false);
	Shape8.addBox(20, 60, 3.5F, -6.2F, -1.7F, 1F, 3F, 7F, 0F, false);
	Shape3.addBox(58, 38, 2.62507F, -6.96184F, -2.91088F, 3F, 2F, 6F, 0F, false);
	Shape15.addBox(63, 46, -0.7F, 1F, -2.3F, 2F, 2F, 5F, -0.1F, false);
	Shape17.addBox(22, 34, 0.3F, -1F, -2.4F, 1F, 3F, 1F, -0.11F, false);
	Shape18.addBox(0, 24, -1.3F, -1F, -2.4F, 1F, 3F, 1F, -0.11F, false);
	Shape16.addBox(63, 27, -1.3F, 1F, -2.6F, 2F, 2F, 5F, -0.1F, false);
	Shape4.addBox(57, 9, -5.62507F, -6.96184F, -2.91088F, 3F, 2F, 6F, 0F, false);
	Shape60.addBox(40, 17, -1F, -0.5F, -0.5F, 2F, 1F, 1F, 0F, false);
	Shape61.addBox(25, 5, -1F, -0.5F, -0.5F, 2F, 1F, 1F, 0F, false);
	body.addBox(0, 11, -4F, 0F, -2F, 8F, 7F, 6F, 0F, false);
	bodydown.addBox(44, 29, -3.5F, 7F, -1.6F, 7F, 4F, 5F, 0F, false);
	Shape10.addBox(25, 0, -3.5F, 0.2F, -2.9F, 7F, 4F, 1F, 0F, false);
	Shape19.addBox(63, 53, -3F, 0.2F, -3.9F, 6F, 1F, 2F, 0F, false);
	Shape20.addBox(64, 63, -2.5F, -0.8F, -3.9F, 5F, 2F, 2F, 0F, false);
	Shape21.addBox(41, 0, -2.5F, -0.8F, -3.9F, 5F, 2F, 2F, 0F, false);
	Shape22.addBox(28, 17, -2.5F, -0.8F, -3.9F, 5F, 2F, 2F, 0F, false);
	Shape11.addBox(0, 74, -1.34679F, -1.12856F, -0.5F, 3F, 2F, 1F, 0.01F, false);
	Shape23.addBox(20, 73, -1.5F, -1F, -0.5F, 3F, 2F, 1F, 0F, false);
	Shape24.addBox(12, 68, -1.34679F, -1.12856F, -0.5F, 3F, 2F, 2F, -0.19F, false);
	Shape25.addBox(15, 24, -1.5F, -1F, -0.5F, 3F, 2F, 2F, -0.2F, false);
	Shape12.addBox(0, 34, -4F, 10F, -2.4F, 8F, 2F, 6F, 0F, false);
	Shape53.addBox(69, 7, -4.75F, -18.33333F, 3.25F, 5F, 1F, 2F, 0F, false);
	Shape54.addBox(0, 33, -0.5F, -0.5F, -1F, 1F, 1F, 2F, 0F, false);
	Shape55.addBox(27, 70, -0.5F, -0.5F, -1F, 1F, 1F, 3F, 0F, false);
	Shape56.addBox(48, 63, -0.5F, -0.5F, -1.5F, 1F, 1F, 3F, 0F, false);
	Shape57.addBox(15, 57, -0.5F, -0.5F, -1.5F, 1F, 1F, 3F, 0F, false);
	Shape58.addBox(35, 48, -0.5F, -0.5F, -1.5F, 1F, 1F, 3F, 0F, false);
	Shape29.addBox(69, 7, -0.25F, -18.33333F, 3.25F, 5F, 1F, 2F, 0F, true);
	Shape34.addBox(0, 33, -0.5F, -0.5F, -1F, 1F, 1F, 2F, 0F, true);
	Shape35.addBox(27, 70, -0.5F, -0.5F, -1F, 1F, 1F, 3F, 0F, true);
	Shape36.addBox(48, 63, -0.5F, -0.5F, -1.5F, 1F, 1F, 3F, 0F, true);
	Shape37.addBox(15, 57, -0.5F, -0.5F, -1.5F, 1F, 1F, 3F, 0F, true);
	Shape38.addBox(35, 48, -0.5F, -0.5F, -1.5F, 1F, 1F, 3F, 0F, true);
	Shape49.addBox(65, 0, -14.725F, -20.825F, 2.6F, 5F, 1F, 2F, 0F, false);
	Shape50.addBox(15, 42, -0.5F, -0.5F, -2.5F, 1F, 1F, 4F, 0F, false);
	Shape51.addBox(0, 11, -0.5F, -0.5F, -1F, 1F, 1F, 2F, 0F, false);
	Shape52.addBox(22, 34, -3.1F, -0.5F, -2.5F, 1F, 1F, 4F, 0F, false);
	Shape39.addBox(65, 0, 9.725F, -20.825F, 2.6F, 5F, 1F, 2F, 0F, true);
	Shape40.addBox(15, 42, -0.5F, -0.5F, -2.5F, 1F, 1F, 4F, 0F, true);
	Shape41.addBox(0, 11, -0.5F, -0.5F, -1F, 1F, 1F, 2F, 0F, true);
	Shape42.addBox(22, 34, 2.1F, -0.5F, -2.5F, 1F, 1F, 4F, 0F, true);
	Shape47.addBox(20, 70, 3.45F, -12.6F, 1.4F, 3F, 1F, 2F, 0F, false);
	Shape48.addBox(0, 0, -0.5F, -0.5F, -1F, 1F, 1F, 2F, 0F, false);
	Shape43.addBox(20, 70, -6.45F, -12.6F, 1.4F, 3F, 1F, 2F, 0F, true);
	Shape44.addBox(0, 0, -0.5F, -0.5F, -1F, 1F, 1F, 2F, 0F, true);
	leftarm.addBox(50, 0, -1F, -2F, -2F, 5F, 4F, 5F, 0F, false);
	rightarm.addBox(0, 24, -4F, -2F, -2F, 5F, 4F, 5F, 0F, false);
	leftleg.addBox(0, 56, -2F, 0F, -3F, 5F, 7F, 5F, 0F, false);
	Shape26.addBox(71, 17, 5.17174F, 3.06176F, -0.2F, 3F, 2F, 1F, -0.19F, false);
	Shape27.addBox(53, 71, -4.49473F, 6.14714F, -0.2F, 3F, 2F, 1F, -0.2F, false);
	legdownl.addBox(0, 42, -2.5F, -0.5F, -2.5F, 5F, 9F, 5F, 0F, false);
	rightleg.addBox(0, 56, -3F, 0F, -3F, 5F, 7F, 5F, 0F, true);
	Shape9.addBox(71, 17, -8.17174F, 3.06176F, -0.2F, 3F, 2F, 1F, -0.19F, true);
	Shape28.addBox(53, 71, 1.49473F, 6.14714F, -0.2F, 3F, 2F, 1F, -0.2F, true);
	legdownr.addBox(0, 42, -2.8F, -0.9F, -0.8F, 5F, 9F, 5F, 0F, true);
	handl1.addBox(43, 43, 15.5F, -2.35F, -1.5F, 5F, 9F, 5F, 0F, false);
	armmiddle.addBox(55, 57, 15.3F, -3.7F, -1.35F, 5F, 1F, 5F, -0.1F, false);
	Shape30.addBox(72, 46, 13.28929F, 6.82413F, -7.6F, 3F, 2F, 1F, -0.19F, false);
	Shape31.addBox(72, 27, -6.79034F, 14.79469F, -7.6F, 3F, 2F, 1F, -0.2F, false);
	handl2.addBox(43, 43, 11.5F, -7.55F, -9.8F, 5F, 9F, 5F, 0F, false);
	armmiddle3.addBox(55, 57, 11.3F, -8.9F, -9.65F, 5F, 1F, 5F, -0.1F, false);
	Shape13.addBox(72, 46, 3.58862F, 8.17583F, -3.6F, 3F, 2F, 1F, -0.19F, false);
	Shape14.addBox(72, 27, -9.80601F, 5.47612F, -3.6F, 3F, 2F, 1F, -0.2F, false);
	handl3.addBox(43, 43, 14.5F, -0.25F, 6.4F, 5F, 9F, 5F, 0F, false);
	armmiddle4.addBox(55, 57, 14.3F, -1.6F, 6.55F, 5F, 1F, 5F, -0.1F, false);
	Shape45.addBox(72, 46, 20.69089F, 3.3548F, -6.6F, 3F, 2F, 1F, -0.19F, false);
	Shape59.addBox(72, 27, -2.08844F, 21.48141F, -6.6F, 3F, 2F, 1F, -0.2F, false);
	handl4.addBox(43, 43, 18.9F, 3.95F, -10.4F, 5F, 9F, 5F, 0F, false);
	armmiddle5.addBox(55, 57, 18.7F, 2.6F, -10.25F, 5F, 1F, 5F, -0.1F, false);
	Shape62.addBox(72, 46, 10.52105F, 17.37102F, -11F, 3F, 2F, 1F, -0.19F, false);
	Shape63.addBox(72, 27, -17.6577F, 13.89996F, -11F, 3F, 2F, 1F, -0.2F, false);
	handl5.addBox(43, 43, 21.6F, -7.65F, 3.6F, 5F, 9F, 5F, 0F, false);
	armmiddle6.addBox(55, 57, 21.4F, -9F, 3.75F, 5F, 1F, 5F, -0.1F, false);
	Shape64.addBox(72, 46, 13.78934F, -0.51413F, -13.7F, 3F, 2F, 1F, -0.19F, false);
	Shape65.addBox(72, 27, 0.52326F, 14.01287F, -13.7F, 3F, 2F, 1F, -0.2F, false);
	handr1.addBox(43, 43, -26.6F, -0.95F, -1.5F, 5F, 9F, 5F, 0F, true);
	armmiddle7.addBox(55, 57, -26.4F, -2.3F, -1.35F, 5F, 1F, 5F, -0.1F, true);
	Shape66.addBox(72, 46, -17.18919F, 7.89659F, -13.7F, 3F, 2F, 1F, -0.19F, true);
	Shape67.addBox(72, 27, 4.69024F, 15.86716F, -13.7F, 3F, 2F, 1F, -0.2F, true);
	handr2.addBox(43, 43, -19.4F, 9.65F, -9.8F, 5F, 9F, 5F, 0F, true);
	armmiddle8.addBox(55, 57, -19.2F, 8.3F, -9.65F, 5F, 1F, 5F, -0.1F, true);
	Shape68.addBox(72, 46, -17.64457F, 21.3518F, -6.5F, 3F, 2F, 1F, -0.19F, true);
	Shape69.addBox(72, 27, 17.86196F, 18.65209F, -6.5F, 3F, 2F, 1F, -0.2F, true);
	handr3.addBox(43, 43, -21.3F, -0.25F, 6.4F, 5F, 9F, 5F, 0F, true);
	armmiddle9.addBox(55, 57, -21.1F, -1.6F, 6.55F, 5F, 1F, 5F, -0.1F, true);
	Shape70.addBox(72, 46, -23.69089F, 3.3548F, -8.4F, 3F, 2F, 1F, -0.19F, true);
	Shape71.addBox(72, 27, -0.91156F, 21.48141F, -8.4F, 3F, 2F, 1F, -0.2F, true);
	handr4.addBox(43, 43, -23.9F, -2.95F, -10.4F, 5F, 9F, 5F, 0F, true);
	armmiddle10.addBox(55, 57, -23.7F, -4.3F, -10.25F, 5F, 1F, 5F, -0.1F, true);
	Shape72.addBox(72, 46, -9.08582F, 12.08531F, -11F, 3F, 2F, 1F, -0.19F, true);
	Shape73.addBox(72, 27, 10.22246F, 8.61426F, -11F, 3F, 2F, 1F, -0.2F, true);
	handr5.addBox(43, 43, -17.2F, -7.65F, -0.5F, 5F, 9F, 5F, 0F, true);
	armmiddle11.addBox(55, 57, -17F, -9F, -0.35F, 5F, 1F, 5F, -0.1F, true);
	Shape74.addBox(72, 46, -13.64856F, 2.1213F, -4.3F, 3F, 2F, 1F, -0.19F, true);
	Shape75.addBox(72, 27, -0.38248F, 11.37744F, -4.3F, 3F, 2F, 1F, -0.2F, true);
	root.addChild(head);
	head.addChild(Shape1);
	head.addChild(Shape6);
	head.addChild(Shape5);
	head.addChild(Shape2);
	head.addChild(Shape46);
	head.addChild(Shape7);
	head.addChild(Shape8);
	head.addChild(glass);
	glass.addChild(Shape3);
	glass.addChild(bone);
	bone.addChild(Shape15);
	bone.addChild(Shape17);
	bone.addChild(Shape18);
	bone.addChild(Shape16);
	glass.addChild(Shape4);
	head.addChild(heart7);
	heart7.addChild(Shape60);
	heart7.addChild(Shape61);
	root.addChild(body);
	body.addChild(bodydown);
	body.addChild(Shape10);
	body.addChild(Shape19);
	body.addChild(Shape20);
	body.addChild(Shape21);
	body.addChild(Shape22);
	body.addChild(heart);
	heart.addChild(Shape11);
	heart.addChild(Shape23);
	body.addChild(heart2);
	heart2.addChild(Shape24);
	heart2.addChild(Shape25);
	body.addChild(Shape12);
	body.addChild(Shape53);
	Shape53.addChild(Shape54);
	Shape53.addChild(Shape55);
	Shape53.addChild(Shape56);
	Shape53.addChild(Shape57);
	Shape53.addChild(Shape58);
	body.addChild(Shape29);
	Shape29.addChild(Shape34);
	Shape29.addChild(Shape35);
	Shape29.addChild(Shape36);
	Shape29.addChild(Shape37);
	Shape29.addChild(Shape38);
	body.addChild(Shape49);
	Shape49.addChild(Shape50);
	Shape49.addChild(Shape51);
	Shape49.addChild(Shape52);
	body.addChild(Shape39);
	Shape39.addChild(Shape40);
	Shape39.addChild(Shape41);
	Shape39.addChild(Shape42);
	body.addChild(Shape47);
	Shape47.addChild(Shape48);
	body.addChild(Shape43);
	Shape43.addChild(Shape44);
	body.addChild(leftarm);
	body.addChild(rightarm);
	body.addChild(leftleg);
	leftleg.addChild(heart3);
	heart3.addChild(Shape26);
	heart3.addChild(Shape27);
	leftleg.addChild(legdownl);
	body.addChild(rightleg);
	rightleg.addChild(heart4);
	heart4.addChild(Shape9);
	heart4.addChild(Shape28);
	rightleg.addChild(legdownr);
	// —— 第一人称拳头子树（viewFirst）——  renderFirst 专用：仅第一人称攻击/治疗态
	// 渲染，第三人称本体（root 树）不包含该子树，故不挂 root、只按 JSON parent 链互挂。
	viewFirst.addChild(left_hands_ro);
	viewFirst.addChild(right_hands_ro);
	left_hands_ro.addChild(handl1);
	left_hands_ro.addChild(handl2);
	left_hands_ro.addChild(handl3);
	left_hands_ro.addChild(handl4);
	left_hands_ro.addChild(handl5);
	handl1.addChild(armmiddle);
	armmiddle.addChild(heart5);
	heart5.addChild(Shape30);
	heart5.addChild(Shape31);
	handl2.addChild(armmiddle3);
	armmiddle3.addChild(heart8);
	heart8.addChild(Shape13);
	heart8.addChild(Shape14);
	handl3.addChild(armmiddle4);
	armmiddle4.addChild(heart9);
	heart9.addChild(Shape45);
	heart9.addChild(Shape59);
	handl4.addChild(armmiddle5);
	armmiddle5.addChild(heart10);
	heart10.addChild(Shape62);
	heart10.addChild(Shape63);
	handl5.addChild(armmiddle6);
	armmiddle6.addChild(heart11);
	heart11.addChild(Shape64);
	heart11.addChild(Shape65);
	right_hands_ro.addChild(handr1);
	right_hands_ro.addChild(handr2);
	right_hands_ro.addChild(handr3);
	right_hands_ro.addChild(handr4);
	right_hands_ro.addChild(handr5);
	handr1.addChild(armmiddle7);
	armmiddle7.addChild(heart12);
	heart12.addChild(Shape66);
	heart12.addChild(Shape67);
	handr2.addChild(armmiddle8);
	armmiddle8.addChild(heart13);
	heart13.addChild(Shape68);
	heart13.addChild(Shape69);
	handr3.addChild(armmiddle9);
	armmiddle9.addChild(heart14);
	heart14.addChild(Shape70);
	heart14.addChild(Shape71);
	handr4.addChild(armmiddle10);
	armmiddle10.addChild(heart15);
	heart15.addChild(Shape72);
	heart15.addChild(Shape73);
	handr5.addChild(armmiddle11);
	armmiddle11.addChild(heart16);
	heart16.addChild(Shape74);
	heart16.addChild(Shape75);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float r, float g,
			float b, float a) {
		renderPart(root, matrices, vertices, light, overlay);
	}

	@Override
	public void renderStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, EntityStandBase entity,
			float ageTicks, float speed, float power) {
		render(matrices, vertices, light, overlay);
		// 攻击/治疗态（power>0）：第三人称也渲染拳头环（不透明），本体只留肩膀
		if (power > 0) {
			renderHandsStand(matrices, vertices, light, overlay, entity, ageTicks, speed, power, 1f);
		}
	}

	/**
	 * 第一人称攻击/治疗态：只渲染 renderFirst 的拳头环（viewFirst 子树），本体隐藏。
	 * 拳头环整体绕 Y 缓慢旋转 + 上下浮动，模拟疯狂钻石拳头乱舞观感。	 * 半透明 alpha 由调用方传入（translucent 层），对齐世界/白金之星式拳头。
	 */
	public void renderHandsStand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			EntityStandBase entity, float ageTicks, float speed, float power, float alpha) {
		// 频率对齐白金之星/世界：speed*4/3（STAR_PLATINUM speed=1.5 → 挥拳频率约 2.0 rad/tick）
		float freq = speed > 0 ? speed * 4.0F / 3.0F : 1.6F;
		float f = ageTicks * freq;
		float pw = power > 0 ? power : 1.0F;
		// 拳头环整体：向前出击/收回（offsetZ 前后快速往复）+ 绕 X 微摆模拟出拳角度变化
		viewFirst.rotateAngleY = 0F;
		viewFirst.rotateAngleX = (float) (Math.sin(f * 3.0) * 0.08);
		viewFirst.offsetZ = (float) (Math.sin(f * 1.5) * 0.22);
		viewFirst.offsetY = (float) (Math.cos(ageTicks * 0.1) * 0.1);
		// 白金之星式十二连打：十拳绕环中心大摆幅挥舞（rotateAngleY 相位错开、幅度 1.1~1.7），
		// 与 setRotationAngles 同相位序列，产生"乱舞连打"而非单纯抽搐
		handl1.rotateAngleY = (float) (Math.cos(f) * 1.2F * pw);
		handl2.rotateAngleY = (float) (Math.cos(f + Math.PI / 3) * 1.4F * pw);
		handl3.rotateAngleY = (float) (Math.cos(f + 2 * Math.PI / 3) * 1.6F * pw);
		handl4.rotateAngleY = (float) (Math.cos(f + 2.5 * Math.PI / 3) * 1.3F * pw);
		handl5.rotateAngleY = (float) (Math.cos(f + 3 * Math.PI / 3) * 1.7F * pw);
		handr1.rotateAngleY = (float) (Math.cos(f + 3.5 * Math.PI / 3) * 1.2F * pw);
		handr2.rotateAngleY = (float) (Math.cos(f + 6 * Math.PI / 3) * 1.1F * pw);
		handr3.rotateAngleY = (float) (Math.cos(f + 8 * Math.PI / 3) * 1.4F * pw);
		handr4.rotateAngleY = (float) (Math.cos(f + 10 * Math.PI / 3) * 1.5F * pw);
		handr5.rotateAngleY = (float) (Math.cos(f + 1.5 * Math.PI / 3) * 1.6F * pw);
		// 白金之星式 setPunch：offsetZ 交替前冲 + 左右分组 offsetX 错位抖动
		float r = (float) Math.random();
		float offysin = (MathHelper.sin(r * speed * ageTicks) * pw - 0.25f) / 16f;
		float offycos = (MathHelper.cos(r * speed * ageTicks) * pw - 0.25f) / 16f;
		handl1.offsetZ = offysin;
		handl2.offsetZ = offycos;
		handl3.offsetZ = offysin;
		handl4.offsetZ = offycos;
		handl5.offsetZ = offysin;
		handr1.offsetZ = offycos;
		handr2.offsetZ = offysin;
		handr3.offsetZ = offycos;
		handr4.offsetZ = offysin;
		handr5.offsetZ = offycos;
		float offxl = (r * MathHelper.sin(r * speed * ageTicks) * pw + 0.2f) / 16f;
		float offxr = (r * MathHelper.sin(r * speed * ageTicks) * pw - 0.2f) / 16f;
		left_hands_ro.offsetX = offxl;
		right_hands_ro.offsetX = offxr;
		// 必须渲染 viewFirst 本身，其旋转/浮动变换才会应用到所有拳头
		viewFirst.render(matrices, vertices, light, overlay, 1f, 1f, 1f, alpha);
	}

	/**
	 * 治疗态粉红罩：不再停在拳头环中心，而是给十拳各渲染一个独立小粉罩，
	 * 紧贴拳头本体并跟随其挥摆（复刻 viewFirst -> hand 的变换链）。
	 * 必须传入 translucent 层的 VertexConsumer，alpha 混合才生效。	 * 第一/第三人称分别由 RenderStandBase 传入各自混合层调用。
	 * 注意：rotationPoint/offset 均为像素单位，当前矩阵栈为格单位，必须 /16。	 * 否则叠加外层 scale(-1,-1,1) 后，正 Y 像素值会被丢到玩家脚底下方。
	 */
	public void renderHalo(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float alpha) {
		if (redHalo == null) {
			return;
		}
		matrices.push();
		// 拳头环整体：viewFirst 锚点 (0,3.45,0.2) + offset 浮动，与 renderHandsStand 同相位。
		// rotationPoint/offset 均为像素单位，ModelPart 内部 translateAndRotate 会再 /16 转格。		// 直接按格传会被外层 scale(-1,-1,1) 翻转为向下，罩子会砸到玩家脚底。
		matrices.translate(viewFirst.offsetX / 16.0F,
				(viewFirst.rotationPointY + viewFirst.offsetY) / 16.0F,
				(viewFirst.rotationPointZ + viewFirst.offsetZ) / 16.0F);
		if (viewFirst.rotateAngleX != 0.0F) {
			// 拳头环整体绕 X 微摆（±0.08rad），罩子同步跟随
			matrices.multiply(RotationAxis.POSITIVE_X.rotation(viewFirst.rotateAngleX));
		}
		// 十拳逐一渲染：旋转跟随拳头绕环中心挥舞，再平移到该拳 addBox 中心
		renderHaloForHand(matrices, vertices, light, overlay, alpha, handl1, 18.0F, 2.15F, 1.0F);
		renderHaloForHand(matrices, vertices, light, overlay, alpha, handl2, 14.0F, -3.05F, -7.3F);
		renderHaloForHand(matrices, vertices, light, overlay, alpha, handl3, 17.0F, 4.25F, 8.9F);
		renderHaloForHand(matrices, vertices, light, overlay, alpha, handl4, 21.4F, 8.45F, -7.9F);
		renderHaloForHand(matrices, vertices, light, overlay, alpha, handl5, 24.1F, -3.15F, 6.1F);
		renderHaloForHand(matrices, vertices, light, overlay, alpha, handr1, -24.1F, 3.55F, 1.0F);
		renderHaloForHand(matrices, vertices, light, overlay, alpha, handr2, -16.9F, 14.15F, -7.3F);
		renderHaloForHand(matrices, vertices, light, overlay, alpha, handr3, -18.8F, 4.25F, 8.9F);
		renderHaloForHand(matrices, vertices, light, overlay, alpha, handr4, -21.4F, 1.55F, -7.9F);
		renderHaloForHand(matrices, vertices, light, overlay, alpha, handr5, -14.7F, -3.15F, 2.0F);
		matrices.pop();
	}

	/**
	 * 单个拳头的小粉罩：完全复刻 ModelPart.translateAndRotate 的 hand 变换链
	 * T(pivot含offset) → Rz → Ry → Rx，再平移到该拳 addBox 中心。
	 * 关键：拳头构造带 rotateAngleX=-90°（setRotation(-90,0,0)），若漏掉只绕 Y 转，
	 * 罩子会在错误平面上转圈、跟不上挥摆的拳头；hand.offsetZ 是像素单位的前冲抖动，
	 * ModelPart 内部会再 /16 转格，这里须同步除。
	 * scale 使 6x6x6 盒放大为约 7x11x7px，比 5x9x5 拳头本体大一圈形成光晕。
	 * heal 贴图粉红区自带淡粉(255,179,255)，乘子略压绿通道让粉更明显。
	 */
	private void renderHaloForHand(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
			float alpha, HAModelPart hand, float cx, float cy, float cz) {
		if (hand == null) {
			return;
		}
		matrices.push();
		// pivot 平移（像素单位，ModelPart 内部 translateAndRotate 会再 /16，这里同步除）
		if (hand.offsetZ != 0.0F) {
			matrices.translate(0.0F, 0.0F, hand.offsetZ / 16.0F);
		}
		if (hand.rotateAngleY != 0.0F) {
			matrices.multiply(RotationAxis.POSITIVE_Y.rotation(hand.rotateAngleY));
		}
		if (hand.rotateAngleX != 0.0F) {
			matrices.multiply(RotationAxis.POSITIVE_X.rotation(hand.rotateAngleX));
		}
		// addBox 中心为像素坐标，转格需 /16
		matrices.translate(cx / 16.0F, cy / 16.0F, cz / 16.0F);
		// 比拳头(5x9x5)大1像素：约 6x10x6px
		matrices.scale(1.0F, 1.67F, 1.0F);
		redHalo.render(matrices, vertices, light, overlay, 1.0F, 0.85F, 1.0F, alpha);
		matrices.pop();
	}

	/** 度转弧度并写入部件旋转角（与 setRotation 同口径）。 */
	private static void setRotation(HAModelPart part, float x, float y, float z) {
		part.rotateAngleX = x * 0.017453292F;
		part.rotateAngleY = y * 0.017453292F;
		part.rotateAngleZ = z * 0.017453292F;
	}
}
