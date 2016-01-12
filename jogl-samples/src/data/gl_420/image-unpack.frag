#version 420 core

#define FRAG_COLOR		0
#define DIFFUSE			0
#define MATERIAL		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = DIFFUSE, r32ui) coherent uniform uimage2D imageData;

layout(binding = MATERIAL) uniform Material
{
    uvec2 imgSize;
} material;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    uint fetch = imageLoad(imageData, ivec2(inBlock.texCoord * material.imgSize)).x;
    color = unpackUnorm4x8(fetch);
}
