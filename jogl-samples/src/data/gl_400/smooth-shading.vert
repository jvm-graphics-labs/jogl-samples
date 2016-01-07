#version 400 core

#define ATTR_POSITION	0
#define ATTR_COLOR		3
#define ATTR_TEXCOORD	4

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;

layout(location = ATTR_POSITION) in vec2 position;
layout(location = ATTR_COLOR) in vec4 color;

out Block
{
    vec4 color;
} outBlock;

void main()
{	
    gl_Position = mvp * vec4(position, 0.0, 1.0);
    outBlock.color = color;
}

