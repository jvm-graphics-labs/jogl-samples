#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

subroutine vec4 Diffuse();

subroutine uniform Diffuse diffuse;
uniform sampler2D diffuseDXT1;
uniform sampler2D diffuseRGB8;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

subroutine(Diffuse)
vec4 diffuseLQ()
{
    return texture(diffuseDXT1, inBlock.texCoord);
}

subroutine(Diffuse)
vec4 diffuseHQ()
{
    return texture(diffuseRGB8, inBlock.texCoord);
}

void main()
{
    color = diffuse();
}
