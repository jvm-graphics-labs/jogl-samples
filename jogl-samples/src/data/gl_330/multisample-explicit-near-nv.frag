#version 330 core
#extension GL_NV_explicit_multisample : require

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform samplerRenderbuffer diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    // integer UV coordinates, needed for fetching multisampled texture
    ivec2 texCoord = ivec2(textureSizeRenderbuffer(diffuse) * inBlock.texCoord);

    color = texelFetchRenderbuffer(diffuse, texCoord, 0);
}
