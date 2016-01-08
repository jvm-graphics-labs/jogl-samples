#version 410 core

#define POSITION		0
#define COLOR			3
#define TEXCOORD		4
#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(location = COLOR) in vec4 color;
layout(location = FRAG_COLOR, index = 0) out vec4 fragColor;

void main()
{
    fragColor = color;
}

