#version 400 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2DArray diffuse[2];
uniform uint diffuseIndex;

in Block
{
    vec2 texCoord;
    float instance;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

vec4 sampling(in sampler2DArray sampler[2], in int layer, in vec2 texCoord)
{
    return texture(sampler[diffuseIndex], vec3(texCoord, layer));
}

void main()
{
    color = sampling(diffuse, 0, inBlock.texCoord);
}
