#version 330 core
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

struct Material
{
    sampler2D diffuse[2];
};

uniform Material material;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    vec4 temp = color * 0.33;

    if ((1.0 - inBlock.texCoord.y) / inBlock.texCoord.x < 1.0)
        temp += texture(material.diffuse[0], inBlock.texCoord) * 0.66;
    else
        temp += texture(material.diffuse[1], inBlock.texCoord) * 0.66;

    color = temp;
}
