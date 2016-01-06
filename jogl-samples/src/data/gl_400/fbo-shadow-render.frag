#version 400 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2DShadow shadow;

in Block
{
    vec4 color;
    vec4 shadowCoord;
} inBlock;

out vec4 color;

void main()
{
    vec4 shadowCoord = inBlock.shadowCoord;
    shadowCoord.z -= 0.005;

    vec4 gather = textureGather(shadow, shadowCoord.xy, shadowCoord.z);
    float texel00 = gather.w;
    float texel10 = gather.z;
    float texel11 = gather.y;
    float texel01 = gather.x;

    vec2 shadowSize = textureSize(shadow, 0);
    vec2 texelCoord = shadowCoord.xy * shadowSize;
    vec2 sampleCoord = fract(texelCoord + 0.5);

    float texel0 = mix(texel00, texel01, sampleCoord.y);
    float texel1 = mix(texel10, texel11, sampleCoord.y);
    float visibility = mix(texel0, texel1, sampleCoord.x);

    color = vec4(mix(vec4(0.5), vec4(1.0), visibility) * inBlock.color);
}
