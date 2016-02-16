#version 420 core

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4
#define FRAG_COLOR		0

#define MATERIAL	0
#define TRANSFORM0	1

in Block
{
    vec2 texCoord;
    flat int drawID;
} inBlock;

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

layout(binding = 0) uniform sampler2D diffuse[3];

void main()
{
    color = texture(diffuse[inBlock.drawID], inBlock.texCoord.st);
}

