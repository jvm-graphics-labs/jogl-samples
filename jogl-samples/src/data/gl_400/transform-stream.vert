#version 400 core

#define POSITION	0
#define COLOR		3
#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform mat4 mvp;

layout(location = POSITION) in vec4 position;

out Block
{
    vec4 color;
} outBlock;

void main()
{	
    gl_Position = mvp * position;
    outBlock.color = vec4(clamp(vec2(position), 0.0, 1.0), 0.0, 1.0);
}
