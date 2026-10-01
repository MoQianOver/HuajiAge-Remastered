package net.minecraft.client.render;

import net.minecraft.util.Identifier;

public final class HuajiagerWingLayerFactory {

	private HuajiagerWingLayerFactory() {
	}

	public static RenderLayer createWingLayer(Identifier texture) {
		return RenderLayer.of(
				"huajiager_lord_wing",
				VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
				VertexFormat.DrawMode.QUADS,
				256,
				RenderLayer.MultiPhaseParameters.builder()
						.texture(new RenderPhase.Texture(texture, false, false))
						.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
						.lightmap(RenderPhase.ENABLE_LIGHTMAP)
						.overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
						.cull(RenderPhase.DISABLE_CULLING)
						.writeMaskState(RenderPhase.ALL_MASK)
						.depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
						.program(new RenderPhase.ShaderProgram(
								GameRenderer::getRenderTypeEntityTranslucentEmissiveProgram))
						.build(true));
	}
}
