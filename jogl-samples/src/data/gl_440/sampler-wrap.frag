#version 440 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
//layout(std430, column_major) buffer; AMD bug

layout(binding = 0) uniform sampler2D diffuse;

in Block
{
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texture(diffuse, inBlock.texCoord);
}
