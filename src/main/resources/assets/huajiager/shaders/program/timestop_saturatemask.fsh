#version 150

uniform sampler2D DiffuseSampler;
uniform float Saturation;
uniform float Radius;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 diffuseColor = texture(DiffuseSampler, texCoord);
    float lum = dot(diffuseColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    float greyScale = lum;
    // dark-area protect: dark unloaded-horizon keeps more original color (avoid crushed black)
    float darkProtect = 1.0 - smoothstep(0.10, 0.35, lum);
    float satEff = clamp(Saturation + (1.0 - Saturation) * darkProtect, 0.0, 1.0);
    vec3 rgbColor = mix(vec3(greyScale), diffuseColor.rgb, satEff);
    // aspect-corrected distance: scale uv.x by W/H so the mask is a true circle
    vec2 uv = texCoord - vec2(0.5);
    vec2 texSize = vec2(textureSize(DiffuseSampler, 0));
    uv.x *= texSize.x / texSize.y;
    float d = length(uv);
    float alpha = 1.0 - smoothstep(Radius - 0.045, Radius + 0.045, d);
    // brightness floor: never crush to pure black
    fragColor = vec4(max(mix(diffuseColor.rgb, rgbColor, alpha), vec3(0.10)), 1.0);
}
