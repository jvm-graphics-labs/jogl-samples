#version 420 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = 0) uniform usampler2D diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    vec2 size = textureSize(diffuse, 0) - 1;

    ivec2 coord = ivec2(inBlock.texCoord * size);
    uvec4 texel = texelFetch(diffuse, coord + ivec2(0, 0), 0);

    color = vec4(texel) / 255.f;

    //color = texture(diffuse, inBlock.texCoord) / 255.f;
}
