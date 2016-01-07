#version 400 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2DArray diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

vec4 trilinearLod(in sampler2DArray sampler, in int layer, in float level, in vec2 texCoord)
{
    int levelMin = int(ceil(level));
    int levelMax = int(floor(level));
    vec2 sizeMin = textureSize(sampler, levelMin).xy - 1;
    vec2 sizeMax = textureSize(sampler, levelMax).xy - 1;
    vec2 texCoordMin = texCoord * sizeMin;
    vec2 texCoordMax = texCoord * sizeMax;
    ivec3 coordMin = ivec3(texCoord * sizeMin, layer);
    ivec3 coordMax = ivec3(texCoord * sizeMax, layer);

    vec4 texelMin00 = texelFetch(sampler, coordMin + ivec3(0, 0, layer), levelMin);
    vec4 texelMin10 = texelFetch(sampler, coordMin + ivec3(1, 0, layer), levelMin);
    vec4 texelMin11 = texelFetch(sampler, coordMin + ivec3(1, 1, layer), levelMin);
    vec4 texelMin01 = texelFetch(sampler, coordMin + ivec3(0, 1, layer), levelMin);

    vec4 texelMax00 = texelFetch(sampler, coordMax + ivec3(0, 0, layer), levelMax);
    vec4 texelMax10 = texelFetch(sampler, coordMax + ivec3(1, 0, layer), levelMax);
    vec4 texelMax11 = texelFetch(sampler, coordMax + ivec3(1, 1, layer), levelMax);
    vec4 texelMax01 = texelFetch(sampler, coordMax + ivec3(0, 1, layer), levelMax);

    vec4 texelMin0 = mix(texelMin00, texelMin01, fract(texCoordMin.y));
    vec4 texelMin1 = mix(texelMin10, texelMin11, fract(texCoordMin.y));
    vec4 texelMin  = mix(texelMin0, texelMin1, fract(texCoordMin.x));

    vec4 texelMax0 = mix(texelMax00, texelMax01, fract(texCoordMax.y));
    vec4 texelMax1 = mix(texelMax10, texelMax11, fract(texCoordMax.y));
    vec4 texelMax  = mix(texelMax0, texelMax1, fract(texCoordMax.x));

    return mix(texelMax, texelMin, fract(level));
}

void main()
{
    vec2 level = textureQueryLod(diffuse, inBlock.texCoord);
    color = trilinearLod(diffuse, 0, level.x, inBlock.texCoord);
}
