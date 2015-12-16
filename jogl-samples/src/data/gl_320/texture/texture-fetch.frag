#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2D diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

out vec4 color;

void main()
{
    vec2 size = textureSize(diffuse, 0) - 1;
    vec2 texCoord = inBlock.texCoord * size;
    ivec2 coord = ivec2(texCoord);

    vec4 texel00 = texelFetch(diffuse, coord + ivec2(0, 0), 0);
    vec4 texel10 = texelFetch(diffuse, coord + ivec2(1, 0), 0);
    vec4 texel11 = texelFetch(diffuse, coord + ivec2(1, 1), 0);
    vec4 texel01 = texelFetch(diffuse, coord + ivec2(0, 1), 0);

    vec2 sampleCoord = fract(texCoord.xy);

    vec4 texel0 = mix(texel00, texel01, sampleCoord.y);
    vec4 texel1 = mix(texel10, texel11, sampleCoord.y);
    color = mix(texel0, texel1, sampleCoord.x);
}
