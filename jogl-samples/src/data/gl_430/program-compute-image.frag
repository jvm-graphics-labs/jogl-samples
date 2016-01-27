#version 420 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

#define SAMPLER_DIFFUSE		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = SAMPLER_DIFFUSE) uniform sampler2D diffuse;

in Block
{
    vec4 texCoord;
    vec4 color;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texture(diffuse, inBlock.texCoord.st) * inBlock.color;
}
