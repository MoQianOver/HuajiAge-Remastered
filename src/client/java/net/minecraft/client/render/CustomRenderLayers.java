package net.minecraft.client.render;

import net.minecraft.util.Identifier;

public final class CustomRenderLayers {
	private CustomRenderLayers() {
	}

	public static RenderLayer entityTranslucentNoCull(Identifier texture) {
		return RenderLayer.of("entity_translucent_no_cull", VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
				VertexFormat.DrawMode.QUADS, 1536, false, false,
				RenderLayer.MultiPhaseParameters.builder()
						.program(new RenderPhase.ShaderProgram(GameRenderer::getRenderTypeEntityTranslucentProgram))
						.texture(new RenderPhase.Texture(texture, false, false))
						.transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
						.lightmap(RenderPhase.ENABLE_LIGHTMAP)
						.overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
						.cull(RenderPhase.DISABLE_CULLING)
						.build(false));
	}
}
