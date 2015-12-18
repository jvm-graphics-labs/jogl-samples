#version 330 core

#define POSITION	0
#define COLOR		3
#define TEXCOORD	4
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;

layout(location = POSITION) in vec2 position;
layout(location = COLOR) in vec4 color;

out Block
{
    vec4 color;
} outBlock;

void main()
{	
    gl_Position = mvp * vec4(position, float(gl_InstanceID) * 0.125 - 0.5, 1.0);
    outBlock.color = color;
}