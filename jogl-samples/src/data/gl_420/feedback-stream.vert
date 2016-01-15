#version 420 core

#define POSITION	0
#define COLOR		3
#define FRAG_COLOR	0

uniform mat4 mvp;

layout(location = POSITION) in vec4 position;
layout(location = COLOR) in vec4 color;

out gl_PerVertex
{
    vec4 gl_Position;
};

out Block
{
    vec4 color;
} outBlock;

void main()
{	
    gl_Position = position + mvp * vec4(0, 0, float(gl_InstanceID) * 0.25 - 0.5, 0);
    outBlock.color = color;
}

