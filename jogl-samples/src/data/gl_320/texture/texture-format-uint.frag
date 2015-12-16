#version 150 core

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform usampler2D diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

out vec4 color;

void main()
{
    vec2 size = textureSize(diffuse, 0) - 1;

    ivec2 coord = ivec2(inBlock.texCoord * size);
    uvec4 texel = texelFetch(diffuse, coord + ivec2(0, 0), 0);

    color = vec4(texel) / 255.f;
}
