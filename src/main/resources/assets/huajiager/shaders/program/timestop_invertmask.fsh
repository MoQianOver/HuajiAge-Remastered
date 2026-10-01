#version 150

uniform sampler2D DiffuseSampler;
uniform float InverseAmount;
uniform float Radius;
uniform float BgGray;
uniform float BgSaturation;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 diffuseColor = texture(DiffuseSampler, texCoord);
    float lum = dot(diffuseColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    // highlight protect: bright sky/sun keeps original color (avoid invert-to-black)
    float protect = smoothstep(0.62, 1.0, lum);
    // dark-area protect: very dark unloaded-horizon void keeps original color (avoid crushed black)
    float darkProtect = 1.0 - smoothstep(0.10, 0.35, lum);
    float eff = InverseAmount * (1.0 - protect) * (1.0 - darkProtect);
    vec3 inverseColor = max(vec3(1.0) - diffuseColor.rgb, vec3(0.25));
    vec3 invColor = mix(diffuseColor.rgb, inverseColor.rgb, eff);
    // outside-circle background: BgGray=1 greys out, else original
    float greyScale = dot(diffuseColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    float bgEff = BgGray * (1.0 - BgSaturation) * (1.0 - darkProtect);
    vec3 bgColor = mix(diffuseColor.rgb, vec3(greyScale), bgEff);
    // aspect-corrected distance: scale uv.x by W/H so the mask is a true circle
    vec2 uv = texCoord - vec2(0.5);
    vec2 texSize = vec2(textureSize(DiffuseSampler, 0));
    uv.x *= texSize.x / texSize.y;
    float d = length(uv);
    float alpha = 1.0 - smoothstep(Radius - 0.045, Radius + 0.045, d);
    // brightness floor: never crush to pure black
    fragColor = vec4(max(mix(bgColor, invColor, alpha), vec3(0.10)), 1.0);
}
