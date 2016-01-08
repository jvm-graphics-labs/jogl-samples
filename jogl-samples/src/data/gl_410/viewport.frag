#version 410 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform sampler2DArray diffuse;

in Block
{
    vec2 texCoord;
    flat int instance;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texture(diffuse, vec3(inBlock.texCoord, inBlock.instance));
}
